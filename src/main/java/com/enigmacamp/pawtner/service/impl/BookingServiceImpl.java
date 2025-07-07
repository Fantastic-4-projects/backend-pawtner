package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.BookingRepository;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.service.BookingService;
import com.enigmacamp.pawtner.service.NotificationService;
import com.enigmacamp.pawtner.service.PaymentService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransCoreApi;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final BusinessRepository businessRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final MidtransCoreApi midtransCoreApi;
    private final NotificationService notificationService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pet pet = petRepository.findById(requestDTO.getPetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        com.enigmacamp.pawtner.entity.Service service = serviceRepository.findById(requestDTO.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        // Calculate delivery fee
        Point userLocation = geometryFactory.createPoint(new Coordinate(requestDTO.getLongitude().doubleValue(), requestDTO.getLatitude().doubleValue()));
        Double distanceInMeters = businessRepository.calculateDistanceToBusiness(service.getBusiness().getId(), userLocation);
        double deliveryFee = calculateDeliveryFee(distanceInMeters);

        Booking booking = Booking.builder()
                .customer(customer)
                .pet(pet)
                .service(service)
                .bookingNumber("BOOK-" + UUID.randomUUID().toString())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .totalPrice(service.getBasePrice().add(BigDecimal.valueOf(deliveryFee))) // Add delivery fee
                .status(BookingStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        // Create payment
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentService.createPayment(payment);

        booking.setSnapToken(payment.getSnapToken());
        booking.setPayment(payment);
        bookingRepository.save(booking);

        return toBookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        return toBookingResponseDTO(booking);
    }

    @Override
    public Page<BookingResponseDTO> getAllBookings(Authentication authentication, Pageable pageable) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().name().equals("CUSTOMER")) {
            return getAllBookingsByCustomer(user.getEmail(), pageable);
        } else if (user.getRole().name().equals("BUSINESS_OWNER")) {
            return getAllBookingsByBusinessOwner(user.getEmail(), pageable);
        } else if (user.getRole().name().equals("ADMIN")) {
            return bookingRepository.findAll(pageable).map(this::toBookingResponseDTO);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    @Override
    public Page<BookingResponseDTO> getAllBookingsByCustomer(String customerEmail, Pageable pageable) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return bookingRepository.findByCustomer(customer, pageable).map(this::toBookingResponseDTO);
    }

    @Override
    public Page<BookingResponseDTO> getAllBookingsByBusinessOwner(String ownerEmail, Pageable pageable) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        List<com.enigmacamp.pawtner.entity.Business> businesses = businessRepository.findAllByOwner_Id(owner.getId());
        if (businesses.isEmpty()) {
            return Page.empty(pageable);
        }

        List<com.enigmacamp.pawtner.entity.Service> services = businesses.stream()
                .flatMap(business -> serviceRepository.findAllByBusiness(business).stream())
                .collect(Collectors.toList());

        if (services.isEmpty()) {
            return Page.empty(pageable);
        }

        return bookingRepository.findByServiceIn(services, pageable).map(this::toBookingResponseDTO);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(UUID id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        booking.setStatus(BookingStatus.valueOf(status.toUpperCase()));
        bookingRepository.save(booking);
        return toBookingResponseDTO(booking);
    }

    @Override
    public void cancelBooking(UUID id, String customerEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to cancel this booking");
        }
        bookingRepository.delete(booking);
    }

    @Override
    public Booking getBookingEntityById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @Override
    public double calculateDeliveryFee(double distanceInMeters) {
        // Example: Base fee + cost per kilometer
        double baseFee = 10000; // Rp 10.000
        double costPerKm = 2000; // Rp 2.000 per kilometer

        // Convert meters to kilometers
        double distanceInKm = distanceInMeters / 1000;

        return baseFee + (costPerKm * distanceInKm);
    }

    @Override
    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        try {
            String bookingNumber = (String) payload.get("order_id");
            JSONObject transactionResult = midtransCoreApi.checkTransaction(bookingNumber);

            String transactionStatus = (String) transactionResult.get("transaction_status");
            String fraudStatus = (String) transactionResult.get("fraud_status");

            Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

            BookingStatus oldStatus = booking.getStatus();
            BookingStatus newStatus = oldStatus;

            if (transactionStatus.equals("capture")) {
                if (fraudStatus.equals("accept")) {
                    newStatus = BookingStatus.CONFIRMED;
                }
            } else if (transactionStatus.equals("settlement")) {
                newStatus = BookingStatus.CONFIRMED;
            } else if (transactionStatus.equals("cancel") || transactionStatus.equals("deny") || transactionStatus.equals("expire")) {
                newStatus = BookingStatus.CANCELLED;
            } else if (transactionStatus.equals("pending")) {
                newStatus = BookingStatus.PENDING_PAYMENT;
            }

            if (oldStatus != newStatus) {
                booking.setStatus(newStatus);
                bookingRepository.save(booking);

                // Update payment status
                Payment payment = booking.getPayment();
                if (payment != null) {
                    if (newStatus == BookingStatus.CONFIRMED) {
                        payment.setStatus(PaymentStatus.SUCCESS);
                    } else if (newStatus == BookingStatus.CANCELLED) {
                        payment.setStatus(PaymentStatus.FAILED);
                    } else if (newStatus == BookingStatus.PENDING_PAYMENT) {
                        payment.setStatus(PaymentStatus.PENDING);
                    }
                    paymentRepository.save(payment);
                }

                // Send notification to customer
                notificationService.sendNotification(
                        booking.getCustomer(),
                        "Booking Status Updated",
                        "Your booking " + booking.getBookingNumber() + " is now " + newStatus.name(),
                        Collections.singletonMap("bookingId", booking.getId().toString())
                );
            }
        } catch (MidtransError e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private BookingResponseDTO toBookingResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customer(mapToUserResponseDTO(booking.getCustomer()))
                .pet(mapToPetResponseDTO(booking.getPet()))
                .petName(booking.getPet().getName())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .businessId(booking.getService().getBusiness().getId())
                .businessName(booking.getService().getBusiness().getName())
                .bookingNumber(booking.getBookingNumber())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice().doubleValue())
                .status(booking.getStatus().name())
                .snapToken(booking.getSnapToken())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhoneNumber())
                .imageUrl(user.getImageUrl())
                .build();
    }

    private PetResponseDTO mapToPetResponseDTO(Pet pet) {
        return PetResponseDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .age(pet.getAge())
                .imageUrl(pet.getImageUrl())
                .notes(pet.getNotes())
                .ownerName(pet.getOwner().getName())
                .build();
    }
}
package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.BookingMapper;
import com.enigmacamp.pawtner.repository.BookingRepository;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.service.BookingService;
import com.enigmacamp.pawtner.service.NotificationService;
import com.enigmacamp.pawtner.service.PaymentService;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.specification.BookingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final BusinessService businessService;
    private final BusinessRepository businessRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

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

        if (service.getCapacityPerDay() != null && service.getCapacityPerDay() > 0) {
            LocalDate bookingDate = requestDTO.getStartTime().toLocalDate();
            LocalDateTime startOfDay = bookingDate.atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            List<BookingStatus> activeStatuses = Arrays.asList(
                    BookingStatus.REQUESTED,
                    BookingStatus.AWAITING_PAYMENT,
                    BookingStatus.CONFIRMED
            );

            long existingBookings = bookingRepository.countActiveBookingsForServiceOnDate(
                    service.getId(),
                    activeStatuses,
                    startOfDay,
                    endOfDay
            );

            if (existingBookings >= service.getCapacityPerDay()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kapasitas layanan penuh untuk tanggal yang dipilih.");
            }
        }
        // Calculate delivery fee
        Point userLocation = geometryFactory.createPoint(new Coordinate(requestDTO.getLongitude().doubleValue(), requestDTO.getLatitude().doubleValue()));
        userLocation.setSRID(4326); // Set SRID to 4326
        Double distanceInMeters = businessRepository.calculateDistanceToBusiness(service.getBusiness().getId(), userLocation);
        double deliveryFee = calculateDeliveryFee(distanceInMeters);
        BigDecimal roundedDeliveryFee = BigDecimal.valueOf(deliveryFee).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (service.getCapacityPerDay() != null && service.getCapacityPerDay() > 0) {
            LocalDate bookingDate = requestDTO.getStartTime().toLocalDate();
            LocalDateTime startOfDay = bookingDate.atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            List<BookingStatus> activeStatuses = Arrays.asList(
                    BookingStatus.REQUESTED,
                    BookingStatus.AWAITING_PAYMENT,
                    BookingStatus.CONFIRMED
            );

            long existingBookings = bookingRepository.countActiveBookingsForServiceOnDate(
                    service.getId(),
                    activeStatuses,
                    startOfDay,
                    endOfDay
            );

            if (existingBookings >= service.getCapacityPerDay()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kapasitas layanan penuh untuk tanggal yang dipilih.");
            }
        }

        // Use MathContext to control precision and rounding for totalPrice
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP); // 10 integer digits + 2 fractional digits = 12 precision
        BigDecimal totalPrice = service.getBasePrice().add(roundedDeliveryFee, mc);

        Booking booking = Booking.builder()
                .customer(customer)
                .pet(pet)
                .service(service)
                .bookingNumber("BOOK-" + UUID.randomUUID().toString())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .totalPrice(totalPrice) // Use the rounded total price
                .totalPrice(service.getBasePrice()) // Simplified for now
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

        return BookingMapper.mapToResponse(booking);
    }

    @Override
    public BookingResponseDTO getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        return BookingMapper.mapToResponse(booking);
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
            return bookingRepository.findAll(pageable).map(BookingMapper::mapToResponse);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    @Override
    public Page<BookingResponseDTO> getAllBookingsByCustomer(String customerEmail, Pageable pageable) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return bookingRepository.findByCustomer(customer, pageable).map(BookingMapper::mapToResponse);
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

        return bookingRepository.findByServiceIn(services, pageable).map(BookingMapper::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> getAllBookingsByBusiness(UUID uuid, String bookingNumber, String nameCustomer, String emailCustomer, BookingStatus bookingStatus,Pageable pageable) {
        businessService.getBusinessByIdForInternal(uuid);
        Specification<Booking> spec = BookingSpecification.getSpecificationByBusiness(uuid, bookingNumber, nameCustomer, emailCustomer, bookingStatus);

        Page<Booking> bookings = bookingRepository.findAll(spec, pageable);
        return bookings.map(BookingMapper::mapToResponse);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(UUID id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        booking.setStatus(BookingStatus.valueOf(status.toUpperCase()));
        bookingRepository.save(booking);
        return BookingMapper.mapToResponse(booking);
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
    public BookingPriceCalculationResponseDTO calculateBookingPrice(UUID serviceId, Double latitude, Double longitude) {
        com.enigmacamp.pawtner.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        userLocation.setSRID(4326); // Set SRID to 4326
        Double distanceInMeters = businessRepository.calculateDistanceToBusiness(service.getBusiness().getId(), userLocation);
        double deliveryFee = calculateDeliveryFee(distanceInMeters);
        BigDecimal roundedDeliveryFee = BigDecimal.valueOf(deliveryFee).setScale(2, BigDecimal.ROUND_HALF_UP);

        MathContext mc = new MathContext(18, RoundingMode.HALF_UP); // Use 18 integer digits for consistency
        BigDecimal totalPrice = service.getBasePrice().add(roundedDeliveryFee, mc);

        return BookingPriceCalculationResponseDTO.builder()
                .serviceId(serviceId)
                .basePrice(service.getBasePrice())
                .deliveryFee(roundedDeliveryFee)
                .totalPrice(totalPrice)
                .build();
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
        String bookingNumber = (String) payload.get("order_id");
        String transactionStatus = (String) payload.get("transaction_status");
        String fraudStatus = (String) payload.get("fraud_status");

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
            if (newStatus == BookingStatus.CONFIRMED) {
                String title = "Pemesanan Anda Dikonfirmasi!";
                String body = String.format("Pemesanan grooming untuk %s pada %s pukul %s telah dikonfirmasi.",
                        booking.getPet().getName(),
                        booking.getStartTime().toLocalDate(),
                        booking.getStartTime().toLocalTime());
                Map<String, String> data = new HashMap<>();
                data.put("type", "BOOKING_CONFIRMATION");
                data.put("id", booking.getId().toString());
                notificationService.sendNotification(booking.getCustomer(), title, body, data);
            } else {
                notificationService.sendNotification(
                        booking.getCustomer(),
                        "Booking Status Updated",
                        "Your booking " + booking.getBookingNumber() + " is now " + newStatus.name(),
                        Collections.singletonMap("bookingId", booking.getId().toString())
                );
            }
        }
    }
}
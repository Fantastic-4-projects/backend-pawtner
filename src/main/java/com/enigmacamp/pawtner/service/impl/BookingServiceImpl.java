package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.BookingRepository;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;

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

        Booking booking = Booking.builder()
                .customer(customer)
                .pet(pet)
                .service(service)
                .bookingNumber("BOOK-" + UUID.randomUUID().toString())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .totalPrice(service.getBasePrice()) // Simplified for now
                .status(BookingStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

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
        // This logic can be expanded based on roles
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return bookingRepository.findAll(pageable).map(this::toBookingResponseDTO);
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
package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.Service;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.BookingRepository;
import com.enigmacamp.pawtner.service.BookingService;
import com.enigmacamp.pawtner.service.PetService;
import com.enigmacamp.pawtner.service.ServiceService;
import com.enigmacamp.pawtner.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final PetService petService;
    private final ServiceService serviceService;

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO, String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        Pet pet = petService.getPetEntityById(bookingRequestDTO.getPetId());
        com.enigmacamp.pawtner.entity.Service service = serviceService.getServiceEntityById(bookingRequestDTO.getServiceId());

        if (!pet.getOwner().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pet does not belong to the authenticated user");
        }

        // Basic availability check (can be expanded)
        if (bookingRequestDTO.getStartTime().isAfter(bookingRequestDTO.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time cannot be after end time");
        }

        // You might want to add more sophisticated availability checks here,
        // e.g., checking service capacity, existing bookings for the time slot.

        Booking booking = Booking.builder()
                .customer(customer)
                .pet(pet)
                .service(service)
                .bookingNumber(generateBookingNumber())
                .startTime(bookingRequestDTO.getStartTime())
                .endTime(bookingRequestDTO.getEndTime())
                .totalPrice(service.getBasePrice())
                .status(BookingStatus.REQUESTED)
                .build();
        bookingRepository.save(booking);

        return mapToResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO getBookingById(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        return mapToResponseDTO(booking);
    }

    @Override
    public Page<BookingResponseDTO> getAllBookingsByCustomerId(String customerEmail, Pageable pageable) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        Page<Booking> bookings = bookingRepository.findByCustomer(customer, pageable);
        return bookings.map(this::mapToResponseDTO);
    }

    @Override
    public Page<BookingResponseDTO> getAllBookingsByBusinessId(UUID businessId, Pageable pageable) {
        // Assuming you have a way to get Business entity by ID
        // For simplicity, we'll create a dummy business object for the query
        // In a real scenario, you'd fetch the Business entity from BusinessService
        // Business business = businessService.getBusinessByIdForInternal(businessId);
        // For now, let's assume the business exists and we can query by its ID directly
        // This might require a custom query in BookingRepository if not directly supported by Spring Data JPA
        // For now, let's filter after fetching all, which is not efficient for large datasets
        // A better approach would be to add a custom query to BookingRepository like:
        // Page<Booking> findByService_Business_Id(UUID businessId, Pageable pageable);

        // Temporarily, to avoid compilation errors, I'll use a placeholder logic.
        // This part needs to be refined based on how you fetch Business entities.
        // For now, I'll just return an empty page or throw an exception if businessService.getBusinessByIdForInternal is not available.
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Fetching bookings by business ID is not yet fully implemented.");
    }

    @Override
    @Transactional
    public BookingResponseDTO updateBookingStatus(Integer id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        try {
            booking.setStatus(BookingStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status: " + status);
        }
        bookingRepository.save(booking);
        return mapToResponseDTO(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Integer id, String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking does not belong to the authenticated user");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a completed or already cancelled booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private String generateBookingNumber() {
        return "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .customerName(booking.getCustomer().getName())
                .petName(booking.getPet().getName())
                .serviceName(booking.getService().getName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

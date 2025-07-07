package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO requestDTO, String customerEmail);
    BookingResponseDTO getBookingById(UUID id);
    Page<BookingResponseDTO> getAllBookings(Authentication authentication, Pageable pageable);
    Page<BookingResponseDTO> getAllBookingsByBusiness(UUID uuid, Pageable pageable);
    Page<BookingResponseDTO> getAllBookingsByCustomer(String customerEmail, Pageable pageable);
    Page<BookingResponseDTO> getAllBookingsByBusinessOwner(String ownerEmail, Pageable pageable);
    BookingResponseDTO updateBookingStatus(UUID id, String status);
    void cancelBooking(UUID id, String customerEmail);
    void handleWebhook(Map<String, Object> payload);
    double calculateDeliveryFee(double distanceInMeters);
    Booking getBookingEntityById(UUID id);
}

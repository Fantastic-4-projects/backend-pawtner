package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO, String customerEmail);
    BookingResponseDTO getBookingById(UUID id);
    Page<BookingResponseDTO> getAllBookingsByCustomerId(String customerEmail, Pageable pageable);
    Page<BookingResponseDTO> getAllBookingsByBusinessId(UUID businessId, Pageable pageable);
    BookingResponseDTO updateBookingStatus(UUID id, String status);
    void cancelBooking(UUID id, String customerEmail);
}
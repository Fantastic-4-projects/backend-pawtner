package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO, String customerEmail);
    BookingResponseDTO getBookingById(Integer id);
    Page<BookingResponseDTO> getAllBookingsByCustomerId(String customerEmail, Pageable pageable);
    Page<BookingResponseDTO> getAllBookingsByBusinessId(UUID businessId, Pageable pageable);
    BookingResponseDTO updateBookingStatus(Integer id, String status);
    void cancelBooking(Integer id, String customerEmail);
}

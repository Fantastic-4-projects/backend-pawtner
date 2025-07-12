package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.BookingService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<BookingResponseDTO>> createBooking(@Valid @RequestBody BookingRequestDTO requestDTO, Authentication authentication) {
        BookingResponseDTO responseDTO = bookingService.createBooking(requestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully created booking", responseDTO);
    }

    @GetMapping("/calculate-price")
    public ResponseEntity<CommonResponse<BookingPriceCalculationResponseDTO>> calculateBookingPrice(
            @RequestParam UUID serviceId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        BookingPriceCalculationResponseDTO responseDTO = bookingService.calculateBookingPrice(serviceId, latitude, longitude);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully calculated booking price", responseDTO);
    }

    @PostMapping("/webhook")
    public ResponseEntity<CommonResponse<String>> handleWebhook(@RequestBody Map<String, Object> payload) {
        bookingService.handleWebhook(payload);
        return ResponseUtil.createResponse(HttpStatus.OK, "Webhook received", null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<BookingResponseDTO>> getBookingById(@PathVariable UUID id) {
        BookingResponseDTO responseDTO = bookingService.getBookingById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched booking", responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<Page<BookingResponseDTO>>> getAllBookings(Authentication authentication, Pageable pageable) {
        Page<BookingResponseDTO> responseDTOPage = bookingService.getAllBookings(authentication, pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all bookings", responseDTOPage);
    }

    @GetMapping("/my-bookings/{businessId}")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<Page<BookingResponseDTO>>> getAllBookingsForBusiness(
            @PathVariable UUID businessId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String nameCustomer,
            @RequestParam(required = false) String emailCustomer,
            @RequestParam(required = false) BookingStatus bookingStatus
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<BookingResponseDTO> responseDTOPage = bookingService.getAllBookingsByBusiness(businessId, orderNumber, nameCustomer, emailCustomer, bookingStatus, pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all bookings for the business", responseDTOPage);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<BookingResponseDTO>> updateBookingStatus(@PathVariable UUID id, @RequestBody String status) {
        BookingResponseDTO responseDTO = bookingService.updateBookingStatus(id, status);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully updated booking status", responseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> cancelBooking(@PathVariable UUID id, Authentication authentication) {
        bookingService.cancelBooking(id, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully cancelled booking", null);
    }
}

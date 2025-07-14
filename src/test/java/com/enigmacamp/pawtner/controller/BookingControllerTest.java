package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/bookings should create a booking and return status 201")
    void createBooking_shouldSucceed() throws Exception {
        BookingRequestDTO request = BookingRequestDTO.builder()
                .petId(UUID.randomUUID())
                .serviceId(UUID.randomUUID())
                .startTime(ZonedDateTime.now().plusDays(1))
                .endTime(ZonedDateTime.now().plusDays(1).plusHours(2))
                .build();
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(UUID.randomUUID())
                .bookingNumber("BOOK-123")
                .status(BookingStatus.AWAITING_PAYMENT.name())
                .build();

        when(bookingService.createBooking(any(BookingRequestDTO.class), eq("customer@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .principal(createMockAuthentication("customer@example.com", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully created booking"))
                .andExpect(jsonPath("$.data.bookingNumber").value("BOOK-123"));
    }

    @Test
    @DisplayName("GET /api/bookings/calculate-price should calculate price and return status 200")
    void calculateBookingPrice_shouldSucceed() throws Exception {
        UUID serviceId = UUID.randomUUID();
        BookingPriceCalculationResponseDTO response = BookingPriceCalculationResponseDTO.builder()
                .serviceId(serviceId)
                .totalPrice(new BigDecimal("75000"))
                .build();

        when(bookingService.calculateBookingPrice(eq(serviceId), anyDouble(), anyDouble())).thenReturn(response);

        mockMvc.perform(get("/api/bookings/calculate-price")
                        .param("serviceId", serviceId.toString())
                        .param("latitude", "-6.200000")
                        .param("longitude", "106.800000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalPrice").value(75000));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} should return booking and status 200")
    void getBookingById_shouldSucceed() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponseDTO response = BookingResponseDTO.builder().id(bookingId).build();

        when(bookingService.getBookingById(bookingId)).thenReturn(response);

        mockMvc.perform(get("/api/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(bookingId.toString()));
    }

    @Test
    @DisplayName("GET /api/bookings should return page of bookings and status 200")
    void getAllBookings_shouldSucceed() throws Exception {
        Page<BookingResponseDTO> bookingPage = new PageImpl<>(Collections.singletonList(new BookingResponseDTO()));
        Authentication auth = createMockAuthentication("user@example.com", "CUSTOMER");

        when(bookingService.getAllBookings(eq(auth), any(Pageable.class))).thenReturn(bookingPage);

        mockMvc.perform(get("/api/bookings").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /api/bookings/{id} should cancel booking and return status 200")
    void cancelBooking_shouldSucceed() throws Exception {
        UUID bookingId = UUID.randomUUID();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");
        doNothing().when(bookingService).cancelBooking(bookingId, auth.getName());

        mockMvc.perform(delete("/api/bookings/{id}", bookingId).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(bookingService, times(1)).cancelBooking(bookingId, auth.getName());
    }

    @Test
    @DisplayName("POST /api/bookings/webhook should receive webhook and return status 200")
    void handleWebhook_shouldSucceed() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "BOOK-123");
        payload.put("transaction_status", "settlement");

        doNothing().when(bookingService).handleWebhook(anyMap());

        mockMvc.perform(post("/api/bookings/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(bookingService, times(1)).handleWebhook(anyMap());
    }
}
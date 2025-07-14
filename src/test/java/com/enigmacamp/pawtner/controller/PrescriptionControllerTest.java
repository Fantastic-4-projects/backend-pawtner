package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.dto.request.PrescriptionRequestDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import com.enigmacamp.pawtner.service.PrescriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrescriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

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
    @DisplayName("POST /api/prescriptions should create a prescription and return status 201")
    void createPrescription_shouldSucceed() throws Exception {
        PrescriptionRequestDTO request = PrescriptionRequestDTO.builder()
                .petId(UUID.randomUUID().toString())
                .bookingId(UUID.randomUUID().toString())
                .issuingBusinessId(UUID.randomUUID().toString())
                .issueDate(LocalDate.now())
                .prescriptionItems(Collections.emptyList())
                .build();
        PrescriptionResponseDTO response = PrescriptionResponseDTO.builder().id(UUID.randomUUID().toString()).build();
        Authentication auth = createMockAuthentication("owner@example.com", "BUSINESS_OWNER");

        when(prescriptionService.createPrescription(any(PrescriptionRequestDTO.class), eq(auth))).thenReturn(response);

        mockMvc.perform(post("/api/prescriptions")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully created prescription"));
    }

    @Test
    @DisplayName("GET /api/prescriptions/{id} should return prescription and status 200")
    void getPrescriptionById_shouldSucceed() throws Exception {
        String prescriptionId = UUID.randomUUID().toString();
        PrescriptionResponseDTO response = PrescriptionResponseDTO.builder().id(prescriptionId).build();

        when(prescriptionService.getPrescriptionById(prescriptionId)).thenReturn(response);

        mockMvc.perform(get("/api/prescriptions/{id}", prescriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(prescriptionId));
    }

    @Test
    @DisplayName("GET /api/prescriptions should return page of prescriptions and status 200")
    void getAllPrescriptions_shouldSucceed() throws Exception {
        Page<PrescriptionResponseDTO> prescriptionPage = new PageImpl<>(Collections.singletonList(new PrescriptionResponseDTO()));
        Authentication auth = createMockAuthentication("user@example.com", "CUSTOMER");

        when(prescriptionService.getAllPrescriptions(eq(auth), any(Pageable.class))).thenReturn(prescriptionPage);

        mockMvc.perform(get("/api/prescriptions").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /api/prescriptions/{id} should delete prescription and return status 200")
    void deletePrescription_shouldSucceed() throws Exception {
        String prescriptionId = UUID.randomUUID().toString();
        doNothing().when(prescriptionService).deletePrescription(prescriptionId);

        mockMvc.perform(delete("/api/prescriptions/{id}", prescriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(prescriptionService, times(1)).deletePrescription(prescriptionId);
    }

    @Test
    @DisplayName("GET /api/prescriptions/booking/{bookingId} should return prescription by booking ID and status 200")
    void getPerceptionByBookingId_shouldSucceed() throws Exception {
        UUID bookingId = UUID.randomUUID();
        Authentication auth = createMockAuthentication("owner@example.com", "BUSINESS_OWNER");
        PrescriptionResponseDTO response = PrescriptionResponseDTO.builder().bookingId(bookingId).build();

        when(prescriptionService.getPerceptionByBookingId(eq(bookingId), eq(auth))).thenReturn(response);

        mockMvc.perform(get("/api/prescriptions/booking/{bookingId}", bookingId).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingId").value(bookingId.toString()));
    }
}
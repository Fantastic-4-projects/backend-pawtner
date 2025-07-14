package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BusinessController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessService businessService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/business/{id} should return business profile and status 200")
    void getBusinessById_shouldReturnProfile_whenIdExists() throws Exception {
        UUID businessId = UUID.randomUUID();

        BusinessResponseDTO mockResponse = BusinessResponseDTO.builder()
                .businessId(businessId)
                .businessName("Test Vet")
                .ownerName("Dr. Huda")
                .build();

        when(businessService.profileBusiness(businessId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/business/{id}", businessId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profil bisnis didapatkan"))
                .andExpect(jsonPath("$.data.businessId").value(businessId.toString()))
                .andExpect(jsonPath("$.data.businessName").value("Test Vet"));
    }
}

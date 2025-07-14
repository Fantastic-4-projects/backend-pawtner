package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.constant.ServiceCategory;
import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.service.ServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceService serviceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create a mock Authentication object
    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/services should create a service and return status 201")
    void createService_shouldCreateService_whenValidDataProvided() throws Exception {
        UUID businessId = UUID.randomUUID();
        ServiceRequestDTO requestDTO = ServiceRequestDTO.builder()
                .businessId(businessId)
                .category(ServiceCategory.GROOMING)
                .name("Pet Grooming")
                .description("Full grooming service for pets")
                .basePrice(BigDecimal.valueOf(150000))
                .capacityPerDay(10)
                .build();

        ServiceResponseDTO mockResponse = ServiceResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Pet Grooming")
                .category(ServiceCategory.GROOMING)
                .basePrice(BigDecimal.valueOf(150000))
                .build();

        MockMultipartFile imagePart = new MockMultipartFile("image", "service.jpg", "image/jpeg", "some-image-bytes".getBytes());

        when(serviceService.createService(any(ServiceRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/services")
                        .file(imagePart)
                        .param("businessId", requestDTO.getBusinessId().toString())
                        .param("category", requestDTO.getCategory().toString())
                        .param("name", requestDTO.getName())
                        .param("description", requestDTO.getDescription())
                        .param("basePrice", requestDTO.getBasePrice().toString())
                        .param("capacityPerDay", requestDTO.getCapacityPerDay().toString())
                        .principal(createMockAuthentication("owner@example.com", "BUSINESS_OWNER")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully created service"))
                .andExpect(jsonPath("$.data.name").value("Pet Grooming"));

        verify(serviceService, times(1)).createService(any(ServiceRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/services/{id} should return service by ID and status 200")
    void getServiceById_shouldReturnService_whenIdExists() throws Exception {
        UUID serviceId = UUID.randomUUID();
        ServiceResponseDTO mockResponse = ServiceResponseDTO.builder()
                .id(serviceId)
                .name("Vaccination")
                .category(ServiceCategory.VETERINARY)
                .build();

        when(serviceService.getServiceById(serviceId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched service"))
                .andExpect(jsonPath("$.data.id").value(serviceId.toString()))
                .andExpect(jsonPath("$.data.name").value("Vaccination"));

        verify(serviceService, times(1)).getServiceById(serviceId);
    }

    @Test
    @DisplayName("GET /api/services should return all services and status 200")
    void getAllServices_shouldReturnAllServices() throws Exception {
        List<ServiceResponseDTO> serviceList = Collections.singletonList(
                ServiceResponseDTO.builder().id(UUID.randomUUID()).name("Service 1").build()
        );
        PageImpl<ServiceResponseDTO> servicePage = new PageImpl<>(serviceList, PageRequest.of(0, 10), 1);

        when(serviceService.getAllServices(any(Pageable.class), any(), any(), any(), any(), any(), any(), any())).thenReturn(servicePage);

        mockMvc.perform(get("/api/services")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched all services"))
                .andExpect(jsonPath("$.data.content[0].name").value("Service 1"));

        verify(serviceService, times(1)).getAllServices(any(Pageable.class), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/services/my-services/{businessId} should return services by business and status 200")
    void getAllServicesByBusiness_shouldReturnServices() throws Exception {
        UUID businessId = UUID.randomUUID();
        List<ServiceResponseDTO> serviceList = Collections.singletonList(
                ServiceResponseDTO.builder().id(UUID.randomUUID()).name("Business Service").build()
        );
        PageImpl<ServiceResponseDTO> servicePage = new PageImpl<>(serviceList, PageRequest.of(0, 10), 1);

        when(serviceService.getAllServicesByBusiness(eq(businessId), any(), any(), any(Pageable.class))).thenReturn(servicePage);

        mockMvc.perform(get("/api/services/my-services/{businessId}", businessId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched all services"))
                .andExpect(jsonPath("$.data.content[0].name").value("Business Service"));

        verify(serviceService, times(1)).getAllServicesByBusiness(eq(businessId), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("PUT /api/services/{id} should update service and return status 200")
    void updateService_shouldUpdateService_whenValidDataProvided() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        ServiceRequestDTO requestDTO = ServiceRequestDTO.builder()
                .id(serviceId)
                .businessId(businessId)
                .category(ServiceCategory.GROOMING)
                .name("Updated Grooming")
                .basePrice(BigDecimal.valueOf(200000))
                .build();

        ServiceResponseDTO mockResponse = ServiceResponseDTO.builder()
                .id(serviceId)
                .name("Updated Grooming")
                .category(ServiceCategory.GROOMING)
                .basePrice(BigDecimal.valueOf(200000))
                .build();

        MockMultipartFile imagePart = new MockMultipartFile("image", "updated_service.jpg", "image/jpeg", "some-updated-image-bytes".getBytes());

        when(serviceService.updateService(any(ServiceRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/services/{id}", serviceId)
                        .file(imagePart)
                        .param("id", requestDTO.getId().toString())
                        .param("businessId", requestDTO.getBusinessId().toString())
                        .param("category", requestDTO.getCategory().toString())
                        .param("name", requestDTO.getName())
                        .param("basePrice", requestDTO.getBasePrice().toString())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .principal(createMockAuthentication("owner@example.com", "BUSINESS_OWNER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully updated service"))
                .andExpect(jsonPath("$.data.name").value("Updated Grooming"));

        verify(serviceService, times(1)).updateService(any(ServiceRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/services/{id} should delete service and return status 200")
    void deleteService_shouldDeleteService_whenAdmin() throws Exception {
        UUID serviceId = UUID.randomUUID();

        doNothing().when(serviceService).deleteService(serviceId);

        mockMvc.perform(delete("/api/services/{id}", serviceId)
                        .principal(createMockAuthentication("owner@example.com", "BUSINESS_OWNER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully deleted service"));

        verify(serviceService, times(1)).deleteService(serviceId);
    }
}
package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.ServiceCategory;
import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Service;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceImplTest {

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private BusinessService businessService;
    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private ServiceServiceImpl serviceService;

    private Service service;
    private Business business;
    private ServiceRequestDTO serviceRequestDTO;

    @BeforeEach
    void setUp() {
        business = Business.builder()
                .id(UUID.randomUUID())
                .name("Test Pet Care")
                .build();

        service = Service.builder()
                .id(UUID.randomUUID())
                .name("Pet Grooming")
                .category(ServiceCategory.GROOMING)
                .basePrice(new BigDecimal("200.00"))
                .capacityPerDay(5)
                .business(business)
                .isActive(true)
                .build();

        serviceRequestDTO = ServiceRequestDTO.builder()
                .businessId(business.getId())
                .name("Pet Grooming")
                .category(ServiceCategory.GROOMING)
                .basePrice(new BigDecimal("200.00"))
                .capacityPerDay(5)
                .image(new MockMultipartFile("image", "grooming.jpg", "image/jpeg", "test data".getBytes()))
                .build();
    }

    @Test
    @DisplayName("createService should return ServiceResponseDTO on success")
    void createService_shouldReturnDTO_onSuccess() throws IOException {
        // Given
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(imageUploadService.upload(any())).thenReturn("http://image.url/grooming.jpg");
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        // When
        ServiceResponseDTO result = serviceService.createService(serviceRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(service.getName());
        verify(businessService).getBusinessByIdForInternal(business.getId());
        verify(imageUploadService).upload(serviceRequestDTO.getImage());
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("getServiceById should return ServiceResponseDTO when service exists")
    void getServiceById_shouldReturnDTO_whenFound() {
        // Given
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        // When
        ServiceResponseDTO result = serviceService.getServiceById(service.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(service.getId());
    }

    @Test
    @DisplayName("getServiceById should throw ResponseStatusException when service not found")
    void getServiceById_shouldThrowException_whenNotFound() {
        // Given
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> serviceService.getServiceById(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("getAllServices should return a page of ServiceResponseDTOs")
    void getAllServices_shouldReturnPageOfDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service> servicePage = new PageImpl<>(Collections.singletonList(service), pageable, 1);
        when(serviceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(servicePage);

        // When
        Page<ServiceResponseDTO> result = serviceService.getAllServices(pageable, null, null, null, null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Pet Grooming");
    }

    @Test
    @DisplayName("deleteService should set isActive to false")
    void deleteService_shouldSetIsActiveToFalse() {
        // Given
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        // When
        serviceService.deleteService(service.getId());

        // Then
        ArgumentCaptor<Service> serviceCaptor = ArgumentCaptor.forClass(Service.class);
        verify(serviceRepository).save(serviceCaptor.capture());
        Service savedService = serviceCaptor.getValue();

        assertThat(savedService.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("deleteService should throw exception when service not found")
    void deleteService_shouldThrowException_whenNotFound() {
        // Given
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class, () -> serviceService.deleteService(UUID.randomUUID()));
        verify(serviceRepository, never()).save(any(Service.class));
    }
}
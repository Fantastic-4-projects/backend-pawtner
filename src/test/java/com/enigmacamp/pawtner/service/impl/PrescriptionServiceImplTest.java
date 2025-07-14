package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.PrescriptionItemRequestDTO;
import com.enigmacamp.pawtner.dto.request.PrescriptionRequestDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.repository.PrescriptionRepository;
import com.enigmacamp.pawtner.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private Pet pet;
    private Business business;
    private Booking booking;
    private Prescription prescription;
    private PrescriptionRequestDTO requestDTO;
    private User businessOwner;

    @BeforeEach
    void setUp() {
        businessOwner = User.builder().id(UUID.randomUUID()).role(UserRole.BUSINESS_OWNER).build();
        pet = Pet.builder().id(UUID.randomUUID()).name("Fido").build();
        business = Business.builder().id(UUID.randomUUID()).name("Vet Clinic").owner(businessOwner).build();
        booking = Booking.builder().id(UUID.randomUUID()).service(Service.builder().business(business).build()).build();

        requestDTO = PrescriptionRequestDTO.builder()
                .petId(pet.getId().toString())
                .bookingId(booking.getId().toString())
                .issuingBusinessId(business.getId().toString())
                .issueDate(LocalDate.now())
                .prescriptionItems(List.of(
                        new PrescriptionItemRequestDTO("Amoxicillin", "50mg", "Once a day", 7, "With food")
                ))
                .build();

        prescription = Prescription.builder()
                .id(UUID.randomUUID())
                .pet(pet)
                .issuingBusiness(business)
                .booking(booking)
                .issueDate(LocalDate.now())
                .prescriptionItems(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("createPrescription should fail if pet not found")
    void createPrescription_shouldFail_whenPetNotFound() {
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.createPrescription(requestDTO, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Pet not found");
    }

    @Test
    @DisplayName("getPrescriptionById should return DTO when found")
    void getPrescriptionById_shouldSucceed() {
        when(prescriptionRepository.findById(any(UUID.class))).thenReturn(Optional.of(prescription));

        PrescriptionResponseDTO result = prescriptionService.getPrescriptionById(prescription.getId().toString());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(prescription.getId().toString());
    }

    @Test
    @DisplayName("getAllPrescriptions for ADMIN should return all prescriptions")
    void getAllPrescriptions_forAdmin_shouldReturnAll() {
        User admin = User.builder().id(UUID.randomUUID()).role(UserRole.ADMIN).build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Prescription> prescriptionPage = new PageImpl<>(Collections.singletonList(prescription));

        when(authentication.getPrincipal()).thenReturn(admin);
        when(prescriptionRepository.findAll(pageable)).thenReturn(prescriptionPage);

        Page<PrescriptionResponseDTO> result = prescriptionService.getAllPrescriptions(authentication, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(prescriptionRepository).findAll(pageable);
        verify(prescriptionRepository, never()).findByPetOwner(any(), any());
        verify(prescriptionRepository, never()).findByIssuingBusinessIn(any(), any());
    }

    @Test
    @DisplayName("getPerceptionByBookingId should return prescription when found")
    void getPerceptionByBookingId_shouldSucceed() {
        when(prescriptionRepository.findByBookingId(any(UUID.class))).thenReturn(Optional.of(prescription));

        PrescriptionResponseDTO result = prescriptionService.getPerceptionByBookingId(booking.getId(), authentication);

        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("getPerceptionByBookingId should throw exception when not found")
    void getPerceptionByBookingId_shouldFail_whenNotFound() {
        when(prescriptionRepository.findByBookingId(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.getPerceptionByBookingId(UUID.randomUUID(), authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Perception not found for this booking ID");
    }
}
package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.PrescriptionRequestDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO, Authentication authentication);
    PrescriptionResponseDTO getPrescriptionById(String id);
    Page<PrescriptionResponseDTO> getAllPrescriptions(Pageable pageable);
    void deletePrescription(String id);
    PrescriptionResponseDTO getPerceptionByBookingId(UUID bookingId, Authentication authentication);
}

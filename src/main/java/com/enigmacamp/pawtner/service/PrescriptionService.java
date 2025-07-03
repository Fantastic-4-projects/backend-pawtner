package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.PrescriptionRequestDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO);
    PrescriptionResponseDTO getPrescriptionById(String id);
    Page<PrescriptionResponseDTO> getAllPrescriptions(Pageable pageable);
    void deletePrescription(String id);
}

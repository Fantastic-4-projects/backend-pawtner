package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.PrescriptionRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import com.enigmacamp.pawtner.service.PrescriptionService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
@AllArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<PrescriptionResponseDTO>> createPrescription(@Valid @RequestBody PrescriptionRequestDTO requestDTO) {
        PrescriptionResponseDTO responseDTO = prescriptionService.createPrescription(requestDTO);
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully created prescription", responseDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN') or hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<PrescriptionResponseDTO>> getPrescriptionById(@PathVariable String id) {
        PrescriptionResponseDTO responseDTO = prescriptionService.getPrescriptionById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched prescription", responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<Page<PrescriptionResponseDTO>>> getAllPrescriptions(Pageable pageable) {
        Page<PrescriptionResponseDTO> responseDTOPage = prescriptionService.getAllPrescriptions(pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all prescriptions", responseDTOPage);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deletePrescription(@PathVariable String id) {
        prescriptionService.deletePrescription(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully deleted prescription", null);
    }
}

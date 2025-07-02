package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.service.ServiceService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@AllArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ServiceResponseDTO>> createService(@Valid @ModelAttribute ServiceRequestDTO serviceRequestDTO) {
        ServiceResponseDTO responseDTO = serviceService.createService(serviceRequestDTO);
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully created service", responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ServiceResponseDTO>> getServiceById(@PathVariable UUID id) {
        ServiceResponseDTO responseDTO = serviceService.getServiceById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched service", responseDTO);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ServiceResponseDTO>>> getAllServices(Pageable pageable) {
        Page<ServiceResponseDTO> responseDTOPage = serviceService.getAllServices(pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all services", responseDTOPage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ServiceResponseDTO>> updateService(@PathVariable UUID id, @Valid @ModelAttribute ServiceRequestDTO serviceRequestDTO) {
        serviceRequestDTO.setId(id);
        ServiceResponseDTO responseDTO = serviceService.updateService(serviceRequestDTO);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully updated service", responseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<Void>> deleteService(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully deleted service", null);
    }
}
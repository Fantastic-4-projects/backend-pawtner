package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.service.ServiceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@AllArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ServiceResponseDTO>> createService(@Valid @ModelAttribute ServiceRequestDTO serviceRequestDTO) {
        ServiceResponseDTO responseDTO = serviceService.createService(serviceRequestDTO);
        CommonResponse<ServiceResponseDTO> commonResponse = new CommonResponse<>(
                "Successfully created service",
                responseDTO
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ServiceResponseDTO>> getServiceById(@PathVariable Integer id) {
        ServiceResponseDTO responseDTO = serviceService.getServiceById(id);
        CommonResponse<ServiceResponseDTO> commonResponse = new CommonResponse<>(
                "Successfully fetched service",
                responseDTO
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ServiceResponseDTO>>> getAllServices(Pageable pageable) {
        Page<ServiceResponseDTO> responseDTOPage = serviceService.getAllServices(pageable);
        CommonResponse<Page<ServiceResponseDTO>> commonResponse = new CommonResponse<>(
                "Successfully fetched all services",
                responseDTOPage
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ServiceResponseDTO>> updateService(@PathVariable Integer id, @Valid @ModelAttribute ServiceRequestDTO serviceRequestDTO) {
        serviceRequestDTO.setId(id);
        ServiceResponseDTO responseDTO = serviceService.updateService(serviceRequestDTO);
        CommonResponse<ServiceResponseDTO> commonResponse = new CommonResponse<>(
                "Successfully updated service",
                responseDTO
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<Void>> deleteService(@PathVariable Integer id) {
        serviceService.deleteService(id);
        CommonResponse<Void> commonResponse = new CommonResponse<>(
                "Successfully deleted service",
                null
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }
}

package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServiceService {
    ServiceResponseDTO createService(ServiceRequestDTO serviceRequestDTO);
    ServiceResponseDTO getServiceById(UUID id);
    Page<ServiceResponseDTO> getAllServices(Pageable pageable);
    ServiceResponseDTO updateService(ServiceRequestDTO serviceRequestDTO);
    void deleteService(UUID id);
    Service getServiceEntityById(UUID id);
}
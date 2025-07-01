package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {
    ServiceResponseDTO createService(ServiceRequestDTO serviceRequestDTO);
    ServiceResponseDTO getServiceById(Integer id);
    Page<ServiceResponseDTO> getAllServices(Pageable pageable);
    ServiceResponseDTO updateService(ServiceRequestDTO serviceRequestDTO);
    void deleteService(Integer id);
}

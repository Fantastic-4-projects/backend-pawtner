package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Product;
import com.enigmacamp.pawtner.entity.Service;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import com.enigmacamp.pawtner.service.ServiceService;
import com.enigmacamp.pawtner.specification.ProductSpecification;
import com.enigmacamp.pawtner.specification.ServiceSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessService businessService;
    private final ImageUploadService imageUploadService;

    @Override
    public ServiceResponseDTO createService(ServiceRequestDTO serviceRequestDTO) {
        Business business = businessService.getBusinessByIdForInternal(serviceRequestDTO.getBusinessId());
        String imageUrl = null;
        if (serviceRequestDTO.getImage() != null && !serviceRequestDTO.getImage().isEmpty()) {
            try {
                imageUrl = imageUploadService.upload(serviceRequestDTO.getImage());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        Service service = Service.builder()
                .business(business)
                .category(serviceRequestDTO.getCategory())
                .name(serviceRequestDTO.getName())
                .basePrice(serviceRequestDTO.getBasePrice())
                .capacityPerDay(serviceRequestDTO.getCapacityPerDay())
                .imageUrl(imageUrl)
                .isActive(true)
                .build();
        serviceRepository.save(service);
        return mapToResponseDTO(service);
    }

    @Override
    public ServiceResponseDTO getServiceById(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        return mapToResponseDTO(service);
    }

    @Override
    public Page<ServiceResponseDTO> getAllServices(Pageable pageable, String name, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Service> spec = ServiceSpecification.getSpecification(name, minPrice, maxPrice);

        Page<Service> services = serviceRepository.findAll(spec, pageable);
        return services.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceResponseDTO> getAllServicesByBusiness(UUID businessId, Pageable pageable) {
        Business business = businessService.getBusinessByIdForInternal(businessId);
        Page<Service> services = serviceRepository.findAllByBusiness(business, pageable);
        return services.map(this::mapToResponseDTO);
    }

    @Override
    public ServiceResponseDTO updateService(ServiceRequestDTO serviceRequestDTO) {
        Service existingService = serviceRepository.findById(serviceRequestDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        String imageUrl = existingService.getImageUrl();
        if (serviceRequestDTO.getImage() != null && !serviceRequestDTO.getImage().isEmpty()) {
            try {
                imageUrl = imageUploadService.upload(serviceRequestDTO.getImage());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        existingService.setCategory(serviceRequestDTO.getCategory());
        existingService.setName(serviceRequestDTO.getName());
        existingService.setBasePrice(serviceRequestDTO.getBasePrice());
        existingService.setCapacityPerDay(serviceRequestDTO.getCapacityPerDay());
        existingService.setImageUrl(imageUrl);

        serviceRepository.save(existingService);
        return mapToResponseDTO(existingService);
    }

    @Override
    public void deleteService(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    private ServiceResponseDTO mapToResponseDTO(Service service) {
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .businessId(service.getBusiness().getId())
                .category(service.getCategory())
                .name(service.getName())
                .basePrice(service.getBasePrice())
                .capacityPerDay(service.getCapacityPerDay())
                .imageUrl(service.getImageUrl())
                .isActive(service.getIsActive())
                .build();
    }

    @Override
    public Service getServiceEntityById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }
}
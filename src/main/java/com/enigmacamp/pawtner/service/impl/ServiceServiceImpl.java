package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.ServiceCategory;
import com.enigmacamp.pawtner.dto.request.ServiceRequestDTO;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Service;
import com.enigmacamp.pawtner.mapper.ServiceMapper;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import com.enigmacamp.pawtner.service.ServiceService;
import com.enigmacamp.pawtner.specification.ServiceSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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

@Slf4j
@Component
@AllArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessService businessService;
    private final ImageUploadService imageUploadService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

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
                .description(serviceRequestDTO.getDescription())
                .basePrice(serviceRequestDTO.getBasePrice())
                .capacityPerDay(serviceRequestDTO.getCapacityPerDay())
                .imageUrl(imageUrl)
                .isActive(true)
                .build();
        serviceRepository.save(service);
        return ServiceMapper.mapToResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponseDTO getServiceById(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        return ServiceMapper.mapToResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceResponseDTO> getAllServices(Pageable pageable, String name, BigDecimal minPrice, BigDecimal maxPrice, Double userLat, Double userLon, Double radiusKm, UUID businessId) {
        Point userLocation = null;
        Double radiusInMeters = null;

        if (userLat != null && userLon != null) {
            userLocation = geometryFactory.createPoint(new Coordinate(userLon, userLat));
            radiusInMeters = (radiusKm != null ? radiusKm : 15.0) * 1000.0;
        }
        Specification<Service> spec = ServiceSpecification.getSpecification(name, minPrice, maxPrice, userLocation, radiusInMeters, businessId);

        Page<Service> services = serviceRepository.findAll(spec, pageable);
        return services.map(ServiceMapper::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceResponseDTO> getAllServicesByBusiness(UUID businessId, String name, ServiceCategory serviceCategory, Pageable pageable) {
        businessService.getBusinessByIdForInternal(businessId);
        Specification<Service> spec = ServiceSpecification.getSpecificationByBusiness(businessId, name, serviceCategory);
        Page<Service> services = serviceRepository.findAll(spec, pageable);
        return services.map(ServiceMapper::mapToResponse);
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
        existingService.setDescription(serviceRequestDTO.getDescription());
        existingService.setBasePrice(serviceRequestDTO.getBasePrice());
        existingService.setCapacityPerDay(serviceRequestDTO.getCapacityPerDay());
        existingService.setImageUrl(imageUrl);

        serviceRepository.save(existingService);
        return ServiceMapper.mapToResponse(existingService);
    }

    @Override
    public void deleteService(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    @Override
    public Service getServiceEntityById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }
}
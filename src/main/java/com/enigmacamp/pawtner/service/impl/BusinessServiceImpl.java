package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.BusinessMapper;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.enigmacamp.pawtner.service.ImageUploadService;
import org.springframework.web.multipart.MultipartFile;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final ImageUploadService imageUploadService;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional(rollbackOn = Exception.class)
    @Override
    public BusinessResponseDTO registerBusiness(BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            String businessImageUrl = null;
            String certificateImageUrl = null;

            if (businessImage != null && !businessImage.isEmpty()) {
                businessImageUrl = imageUploadService.upload(businessImage);
            }

            if (certificateImage != null && !certificateImage.isEmpty()) {
                certificateImageUrl = imageUploadService.upload(certificateImage);
            }

            Business newBusiness = Business.builder()
                    .owner(currentUser)
                    .name(businessRequestDTO.getNameBusiness())
                    .description(businessRequestDTO.getDescriptionBusiness())
                    .address(businessRequestDTO.getBusinessAddress())
                    .businessType(businessRequestDTO.getBusinessType())
                    .hasEmergencyServices(businessRequestDTO.getHasEmergencyServices())
                    .businessEmail(businessRequestDTO.getBusinessEmail())
                    .businessPhone(businessRequestDTO.getBusinessPhone())
                    .emergencyPhone(businessRequestDTO.getEmergencyPhone())
                    .businessImageUrl(businessImageUrl) // boleh null
                    .certificateImageUrl(certificateImageUrl) // boleh null
                    .statusRealtime(businessRequestDTO.getBusinessStatus())
                    .operationHours(businessRequestDTO.getOperationHours())
                    .location(geometryFactory.createPoint(new Coordinate(businessRequestDTO.getLongitude().doubleValue(), businessRequestDTO.getLatitude().doubleValue())))
                    .build();

           return BusinessMapper.mapToResponse(businessRepository.save(newBusiness));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }

    @Override
    public BusinessResponseDTO profileBusiness(UUID businessId) {
        Business business = getBusinessByIdForInternal(businessId);
        return BusinessMapper.mapToResponse(business);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public BusinessResponseDTO updateBusiness(UUID businessId, BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage) {
        Business business = getBusinessByIdForInternal(businessId);

        try {
            String businessImageUrl = business.getBusinessImageUrl();
            String certificateImageUrl = business.getCertificateImageUrl();

            if (businessImage != null && !businessImage.isEmpty()) {
                businessImageUrl = imageUploadService.upload(businessImage);
            }

            if (certificateImage != null && !certificateImage.isEmpty()) {
                certificateImageUrl = imageUploadService.upload(certificateImage);
            }

            business.setName(businessRequestDTO.getNameBusiness());
            business.setDescription(businessRequestDTO.getDescriptionBusiness());
            business.setBusinessType(businessRequestDTO.getBusinessType());
            business.setHasEmergencyServices(businessRequestDTO.getHasEmergencyServices());
            business.setBusinessEmail(businessRequestDTO.getBusinessEmail());
            business.setBusinessPhone(businessRequestDTO.getBusinessPhone());
            business.setEmergencyPhone(businessRequestDTO.getEmergencyPhone());
            business.setAddress(businessRequestDTO.getBusinessAddress());
            business.setStatusRealtime(businessRequestDTO.getBusinessStatus());
            business.setOperationHours(businessRequestDTO.getOperationHours());
            business.setLocation(geometryFactory.createPoint(new Coordinate(businessRequestDTO.getLongitude().doubleValue(), businessRequestDTO.getLatitude().doubleValue())));
            business.setBusinessImageUrl(businessImageUrl);
            business.setCertificateImageUrl(certificateImageUrl);

            businessRepository.save(business);
            return BusinessMapper.mapToResponse(business);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }



    @Override
    public List<BusinessResponseDTO> viewBusiness() {
        return businessRepository.findAll()
                .stream().map(BusinessMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessResponseDTO> viewMyBusiness() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("currentUser: {}", currentUser.getUsername());
        return businessRepository.findAllByOwner_Id(currentUser.getId())
                .stream()
                .filter(Business::getIsActive)
                .map(BusinessMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Business getBusinessByIdForInternal(UUID id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));
    }

    @Override
    public BusinessResponseDTO approveBusiness(UUID businessId, Boolean approved) {
        Business business = getBusinessByIdForInternal(businessId);

        business.setIsApproved(approved);
        businessRepository.save(business);

        return BusinessMapper.mapToResponse(business);
    }

    @Override
    public BusinessResponseDTO openBusiness(UUID businessId, BusinessRequestDTO businessRequestDTO) {
        Business business = getBusinessByIdForInternal(businessId);

        business.setStatusRealtime(businessRequestDTO.getBusinessStatus());
        businessRepository.save(business);

        return BusinessMapper.mapToResponse(business);
    }

    @Override
    public List<BusinessResponseDTO> findNearbyBusinesses(double lat, double lon, double radiusKm, Boolean hasEmergencyServices, String statusRealtime) {
        Point userLocation = geometryFactory.createPoint(new Coordinate(lon, lat));
        double distanceInMeters = radiusKm * 1000;
        return businessRepository.findNearbyBusinessesWithFilters(userLocation, distanceInMeters, hasEmergencyServices, statusRealtime)
                .stream().map(BusinessMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBusiness(UUID businessId) {
        Business business = getBusinessByIdForInternal(businessId);
        business.setIsActive(false);
        businessRepository.save(business);
    }

    @Override
    public Business getBusinessByOwnerEmailForInternal(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return businessRepository.findByOwner(owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found for this owner"));
    }
}
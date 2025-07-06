package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.enigmacamp.pawtner.service.ImageUploadService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final ImageUploadService imageUploadService;
    private final UserRepository userRepository;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void registerBusiness(BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage) {
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
                    .businessEmail(businessRequestDTO.getBusinessEmail())
                    .businessPhone(businessRequestDTO.getBusinessPhone())
                    .businessImageUrl(businessImageUrl) // boleh null
                    .certificateImageUrl(certificateImageUrl) // boleh null
                    .latitude(businessRequestDTO.getLatitude())
                    .longitude(businessRequestDTO.getLongitude())
                    .operationHours(businessRequestDTO.getOperationHours())
                    .build();

            businessRepository.save(newBusiness);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }

    @Override
    public BusinessResponseDTO profileBusiness(UUID businessId) {
        Business business = getBusinessByIdForInternal(businessId);
        return mapToResponse(business);
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
            business.setBusinessEmail(businessRequestDTO.getBusinessEmail());
            business.setBusinessPhone(businessRequestDTO.getBusinessPhone());
            business.setEmergencyPhone(businessRequestDTO.getEmergencyPhone());
            business.setAddress(businessRequestDTO.getBusinessAddress());
            business.setLatitude(businessRequestDTO.getLatitude());
            business.setLongitude(businessRequestDTO.getLongitude());
            business.setBusinessImageUrl(businessImageUrl);
            business.setCertificateImageUrl(certificateImageUrl);

            businessRepository.save(business);
            return mapToResponse(business);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }



    @Override
    public List<BusinessResponseDTO> viewBusiness() {
        return businessRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessResponseDTO> viewMyBusiness() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return businessRepository.findAllByOwner_Id(currentUser.getId())
                .stream().map(this::mapToResponse)
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

        return mapToResponse(business);
    }

    @Override
    public BusinessResponseDTO openBusiness(UUID businessId) {
        Business business = getBusinessByIdForInternal(businessId);

        business.setIsOpen(!business.getIsOpen());
        businessRepository.save(business);

        return mapToResponse(business);
    }

    @Override
    public void deleteBusiness(UUID businessId) {
        Business business = getBusinessByIdForInternal(businessId);
        businessRepository.delete(business);
    }

    @Override
    public Business getBusinessByOwnerEmailForInternal(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return businessRepository.findByOwner(owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found for this owner"));
    }

    private BusinessResponseDTO mapToResponse(Business business) {
        return BusinessResponseDTO.builder()
                .businessId(business.getId())
                .ownerName(business.getOwner().getName())
                .businessName(business.getName())
                .businessAddress(business.getAddress())
                .operationHours(business.getOperationHours())
                .isOpen(business.getIsOpen())
                .statusApproved(
                        business.getIsApproved() == null ? "Pending"
                                : business.getIsApproved() ? "Approved"
                                : "Rejected"
                )
                .build();
    }
}
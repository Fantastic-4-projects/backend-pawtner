package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Transactional(rollbackOn =  Exception.class)
    @Override
    public void registerBusiness(BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            String businessImageUrl = imageUploadService.upload(businessImage);
            String certificateImageUrl = imageUploadService.upload(certificateImage);

            Business newBusiness = Business.builder()
                    .owner(currentUser)
                    .name(businessRequestDTO.getNameBusiness())
                    .description(businessRequestDTO.getDescriptionBusiness())
                    .address(businessRequestDTO.getBusinessAddress())
                    .businessType(businessRequestDTO.getBusinessType())
                    .businessEmail(businessRequestDTO.getBusinessEmail())
                    .businessPhone(businessRequestDTO.getBusinessPhone())
                    .businessImageUrl(businessImageUrl)
                    .certificateImageUrl(certificateImageUrl)
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
        Business business = businessRepository.findBusinessById((businessId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));

        business.setIsApproved(approved);
        businessRepository.save(business);

        return mapToResponse(business);
    }

    private BusinessResponseDTO mapToResponse(Business business) {
        return BusinessResponseDTO.builder()
                .businessId(business.getId())
                .ownerName(business.getOwner().getName())
                .businessName(business.getName())
                .businessAddress(business.getAddress())
                .operationHours(business.getOperationHours())
                .statusApproved(
                        business.getIsApproved() == null ? "Pending"
                                : business.getIsApproved() ? "Approved"
                                : "Rejected"
                )
                .build();
    }
}
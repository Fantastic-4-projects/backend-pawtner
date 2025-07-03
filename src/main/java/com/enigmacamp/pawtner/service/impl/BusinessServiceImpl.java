package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
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

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;

    @Transactional(rollbackOn =  Exception.class)
    @Override
    public BusinessResponseDTO registerBusiness(BusinessRequestDTO businessRequestDTO) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Business newBusiness = Business.builder()
                .owner(currentUser)
                .name(businessRequestDTO.getNameBusiness())
                .description(businessRequestDTO.getDescriptionBusiness())
                .address(businessRequestDTO.getBusinessAddress())
                .businessType(businessRequestDTO.getBusinessType())
                .businessEmail(businessRequestDTO.getBusinessEmail())
                .businessPhone(businessRequestDTO.getBusinessPhone())
                .businessImageUrl(businessRequestDTO.getBusinessImageUrl())
                .certificateImageUrl(businessRequestDTO.getCertificateImageUrl())
                .latitude(businessRequestDTO.getLatitude())
                .longitude(businessRequestDTO.getLongitude())
                .operationHours(businessRequestDTO.getOperationHours())
                .build();

        businessRepository.save(newBusiness);

        return mapToResponse(newBusiness);
    }

    @Override
    public List<BusinessResponseDTO> viewBusiness() {
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
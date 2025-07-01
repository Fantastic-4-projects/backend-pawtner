package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.model.OperationHoursDTO;
import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final AuthRepository authRepository;

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

        currentUser.setRole(UserRole.BUSINESS_OWNER);

        businessRepository.save(newBusiness);
        authRepository.save(currentUser);

        return mapToResponse(newBusiness);
    }

    @Override
    public List<BusinessResponseDTO> viewBusiness() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return businessRepository.findAllByOwner_Id(currentUser.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BusinessResponseDTO mapToResponse(Business business) {
        return BusinessResponseDTO.builder()
                .ownerName(business.getOwner().getName())
                .businessName(business.getName())
                .businessAddress(business.getAddress())
                .operationHours(business.getOperationHours())
                .build();
    }
}

package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;

import java.math.BigDecimal;

public class BusinessMapper {
    public static BusinessResponseDTO mapToResponse(Business business) {
        return BusinessResponseDTO.builder()
                .businessId(business.getId())
                .ownerName(business.getOwner().getName())
                .businessName(business.getName())
                .description(business.getDescription())
                .businessType(business.getBusinessType())
                .hasEmergencyServices(business.getHasEmergencyServices())
                .businessEmail(business.getBusinessEmail())
                .businessPhone(business.getBusinessPhone())
                .emergencyPhone(business.getEmergencyPhone())
                .businessImageUrl(business.getBusinessImageUrl())
                .certificateImageUrl(business.getCertificateImageUrl())
                .latitude(BigDecimal.valueOf(business.getLocation().getY()))
                .longitude(BigDecimal.valueOf(business.getLocation().getX()))
                .statusRealTime(business.getStatusRealtime())
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

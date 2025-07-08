package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;

import java.math.BigDecimal;

public class BusinessMapper {
    public static BusinessResponseDTO mapToResponse(Business business) {
        BigDecimal latitude = null;
        BigDecimal longitude = null;

        if (business == null) {
            return null; // atau lempar exception custom kalau wajib ada
        }

        if (business.getLocation() != null) {
            latitude = BigDecimal.valueOf(business.getLocation().getY());
            longitude = BigDecimal.valueOf(business.getLocation().getX());
        }

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
                .latitude(latitude)
                .longitude(longitude)
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

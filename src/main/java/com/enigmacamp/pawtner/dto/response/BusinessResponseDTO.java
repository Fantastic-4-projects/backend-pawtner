package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.constant.BusinessStatus;
import com.enigmacamp.pawtner.constant.BusinessType;
import com.enigmacamp.pawtner.dto.request.OperationHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class BusinessResponseDTO {
    private UUID businessId;
    private String ownerName;
    private String businessName;
    private String description;
    private BusinessType businessType;
    private Boolean hasEmergencyServices;
    private String businessEmail;
    private String businessPhone;
    private String emergencyPhone;
    private String businessImageUrl;
    private String certificateImageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BusinessStatus statusRealTime;
    private String businessAddress;
    private OperationHoursDTO operationHours;
    private String statusApproved = "Pending";
}
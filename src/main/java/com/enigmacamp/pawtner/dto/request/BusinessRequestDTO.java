package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.BusinessType;
import com.enigmacamp.pawtner.dto.request.OperationHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessRequestDTO {
    private String nameBusiness;
    private String descriptionBusiness;
    private BusinessType businessType;
    private String businessEmail;
    private String businessPhone;
    private String emergencyPhone;
    private String businessImageUrl;
    private String certificateImageUrl;
    private String businessAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OperationHoursDTO operationHours;
}

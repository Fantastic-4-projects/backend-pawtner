package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

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
    private Map<String, String> operationHour;
}

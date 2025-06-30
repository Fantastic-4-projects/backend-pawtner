package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessResponseDTO {
    private String ownerName;
    private String businessName;
    private String businessAddress;
    private Map<String, String> operationHours;
}

package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.dto.request.OperationHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessResponseDTO {
    private UUID businessId;
    private String ownerName;
    private String businessName;
    private String businessAddress;
    private OperationHoursDTO operationHours;
}

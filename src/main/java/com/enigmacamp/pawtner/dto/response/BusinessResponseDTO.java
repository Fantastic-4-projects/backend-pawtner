package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.model.OperationHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessResponseDTO {
    private String ownerName;
    private String businessName;
    private String businessAddress;
    private OperationHoursDTO operationHours;
}

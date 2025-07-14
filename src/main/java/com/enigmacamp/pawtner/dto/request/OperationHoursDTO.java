package com.enigmacamp.pawtner.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationHoursDTO {
    @Builder.Default
    private Object monday = "Closed";
    @Builder.Default
    private Object tuesday = "Closed";
    @Builder.Default
    private Object wednesday = "Closed";
    @Builder.Default
    private Object thursday = "Closed";
    @Builder.Default
    private Object friday = "Closed";
    @Builder.Default
    private Object saturday = "Closed";
    @Builder.Default
    private Object sunday = "Closed";
}


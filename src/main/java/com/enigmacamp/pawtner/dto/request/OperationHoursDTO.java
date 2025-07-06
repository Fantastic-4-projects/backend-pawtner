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
    private String monday = "Closed";
    @Builder.Default
    private String tuesday = "Closed";
    @Builder.Default
    private String wednesday = "Closed";
    @Builder.Default
    private String thursday = "Closed";
    @Builder.Default
    private String friday = "Closed";
    @Builder.Default
    private String saturday = "Closed";
    @Builder.Default
    private String sunday = "Closed";
}
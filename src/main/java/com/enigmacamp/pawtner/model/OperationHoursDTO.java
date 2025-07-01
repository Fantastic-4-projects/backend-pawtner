package com.enigmacamp.pawtner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationHoursDTO {
    private String monday = "Closed";
    private String tuesday = "Closed";
    private String wednesday = "Closed";
    private String thursday = "Closed";
    private String friday = "Closed";
    private String saturday = "Closed";
    private String sunday = "Closed";
}

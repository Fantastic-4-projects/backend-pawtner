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

    private TimeSlot monday;
    private TimeSlot tuesday;
    private TimeSlot wednesday;
    private TimeSlot thursday;
    private TimeSlot friday;
    private TimeSlot saturday;
    private TimeSlot sunday;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private String open;
        private String close;
    }
}

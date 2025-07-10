package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.PrescriptionItemResponseDTO;
import com.enigmacamp.pawtner.entity.PrescriptionItem;

public class PrescriptionItemMapper {
    public static PrescriptionItemResponseDTO mapToResponse(PrescriptionItem prescriptionItem) {
        return PrescriptionItemResponseDTO.builder()
                .id(prescriptionItem.getId().toString())
                .medicationName(prescriptionItem.getMedicationName())
                .dosage(prescriptionItem.getDosage())
                .frequency(prescriptionItem.getFrequency())
                .durationDays(prescriptionItem.getDurationDays())
                .instructions(prescriptionItem.getInstructions())
                .build();
    }
}

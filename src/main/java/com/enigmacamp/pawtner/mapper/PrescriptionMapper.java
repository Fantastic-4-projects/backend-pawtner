package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.*;
import com.enigmacamp.pawtner.entity.Prescription;
import com.enigmacamp.pawtner.entity.PrescriptionItem;

import java.util.List;
import java.util.stream.Collectors;

public class PrescriptionMapper {

    public static PrescriptionResponseDTO mapToResponse(Prescription prescription) {
        List<PrescriptionItemResponseDTO> itemDTOs = prescription.getPrescriptionItems().stream()
                .map(PrescriptionItemMapper::mapToResponse)
                .collect(Collectors.toList());

        return PrescriptionResponseDTO.builder()
                .id(prescription.getId().toString())
                .pet(PetResponseDTO.builder()
                        .id(prescription.getPet().getId())
                        .name(prescription.getPet().getName())
                        .build())
                .issuingBusiness(BusinessResponseDTO.builder()
                        .businessId(prescription.getIssuingBusiness().getId())
                        .businessName(prescription.getIssuingBusiness().getName())
                        .build())
                .bookingId(prescription.getBooking() != null ? prescription.getBooking().getId() : null)
                .issueDate(prescription.getIssueDate())
                .notes(prescription.getNotes())
                .prescriptionItems(itemDTOs)
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}

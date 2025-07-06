package com.enigmacamp.pawtner.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionRequestDTO {
    @NotBlank(message = "Pet ID is required")
    private String petId;

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotBlank(message = "Issuing Business ID is required")
    private String issuingBusinessId;

    @NotNull(message = "Issue Date is required")
    private LocalDate issueDate;

    private String notes;

    @NotNull
    @Valid
    private List<PrescriptionItemRequestDTO> prescriptionItems;
}

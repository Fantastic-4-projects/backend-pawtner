package com.enigmacamp.pawtner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApproveBusinessRequestDTO {
    private Boolean approve;

    @NotBlank(message = "Alasan tidak boleh kosong. Berikan masukan yang membangun untuk pemilik bisnis.")
    private String reason;
}
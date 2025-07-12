package com.enigmacamp.pawtner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserStatusRequestDTO {
    @NotBlank(message = "Aksi tidak boleh kosong (contoh: 'ban' atau 'suspend')")
    private String action;

    @NotNull(message = "Nilai status tidak boleh kosong (true/false)")
    private Boolean value;

    @NotNull(message = "Nilai tidak boleh kosong")
    private Boolean isSend;

    @NotBlank(message = "Alasan tidak boleh kosong")
    private String reason;
}
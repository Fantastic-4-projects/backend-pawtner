package com.enigmacamp.pawtner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Token tidak boleh kosong")
    private String token;

    @NotBlank(message = "Password baru tidak boleh kosong")
    @Size(min = 6, message = "Password harus lebih dar 6 karakter")
    private String newPassword;
}

package com.enigmacamp.pawtner.dto.request;

import lombok.Data;

@Data
public class VerificationRequestDTO {
    private String email;
    private String verificationCode;
}

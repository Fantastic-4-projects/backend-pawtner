package com.enigmacamp.pawtner.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RegisterResponseDTO {
    String email;
}

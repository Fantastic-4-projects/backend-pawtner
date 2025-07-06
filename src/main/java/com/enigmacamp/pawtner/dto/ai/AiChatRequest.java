package com.enigmacamp.pawtner.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "Pesan tidak boleh kosong")
        String message
) {}
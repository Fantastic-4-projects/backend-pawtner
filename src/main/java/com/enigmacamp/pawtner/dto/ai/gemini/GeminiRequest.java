package com.enigmacamp.pawtner.dto.ai.gemini;

public record GeminiRequest(Content[] contents) {
    public static record Content(Part[] parts) {}
    public static record Part(String text) {}
}

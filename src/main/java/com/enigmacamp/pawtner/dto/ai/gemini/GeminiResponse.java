package com.enigmacamp.pawtner.dto.ai.gemini;

public record GeminiResponse(Candidate[] candidates, PromptFeedback promptFeedback) {
    public static record Candidate(Content content) {}
    public static record Content(Part[] parts) {}
    public static record Part(String text) {}
    public static record PromptFeedback(String blockReason) {}
}

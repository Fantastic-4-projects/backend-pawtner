package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.ai.AiChatRequest;
import com.enigmacamp.pawtner.dto.ai.AiChatResponse;
import com.enigmacamp.pawtner.service.GeminiAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiAiService geminiAiService;

    @PostMapping("/chat")
    public Mono<ResponseEntity<AiChatResponse>> handleChat(@RequestBody @Valid AiChatRequest request) {
        return geminiAiService.getChatResponse(request.message())
                .map(reply -> ResponseEntity.ok(new AiChatResponse(reply)))
                .onErrorResume(e -> Mono.just(
                                ResponseEntity.status(500).body(new AiChatResponse("Maaf, terjadi kesalahan internal saat berkomunikasi dengan AI."))
                        )
                );
    }
}
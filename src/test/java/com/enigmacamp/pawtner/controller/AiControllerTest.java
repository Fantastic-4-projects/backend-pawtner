package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.dto.ai.AiChatRequest;
import com.enigmacamp.pawtner.service.GeminiAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeminiAiService geminiAiService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/ai/chat should return 400 Bad Request for blank message")
    void handleChat_shouldReturnBadRequest_whenMessageIsBlank() throws Exception {
        AiChatRequest request = new AiChatRequest("");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(geminiAiService, never()).getChatResponse(anyString());
    }
}
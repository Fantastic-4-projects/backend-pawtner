package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.ai.gemini.GeminiRequest;
import com.enigmacamp.pawtner.dto.ai.gemini.GeminiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String modelName;
    private final ObjectMapper objectMapper; // <-- PERUBAHAN 1: Menambahkan ObjectMapper

    public GeminiAiService(
            WebClient.Builder webClientBuilder,
            @Value("${pawtner.ai.gemini.api-url:https://generativelanguage.googleapis.com/}") String apiUrl,
            @Value("${pawtner.ai.gemini.api-key}") String apiKey,
            @Value("${pawtner.ai.gemini.model-name}") String modelName,
            ObjectMapper objectMapper // <-- PERUBAHAN 2: Inject ObjectMapper melalui konstruktor
    ) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.objectMapper = objectMapper;
    }

    private final Set<String> seriousKeywords = new HashSet<>(Arrays.asList(
            "sakit", "darurat", "gejala", "muntah", "lesu", "nyeri", "luka",
            "pendarahan", "demam", "kejang", "tidak mau makan", "tidak mau minum",
            "perilaku aneh", "dokter hewan", "klinik", "medis", "obat", "diagnosa",
            "beracun", "keracunan"
    ));


    public Mono<String> getChatResponse(String userMessage) {

        String lowerCaseUserMessage = userMessage.toLowerCase();

        for (String keyword : seriousKeywords) {
            if (lowerCaseUserMessage.contains(keyword)) {
                log.info("Pertanyaan serius terdeteksi dengan kata kunci: '{}'", keyword);
                return Mono.just(
                        "Pertanyaan Anda tampaknya berkaitan dengan kondisi kesehatan serius. " +
                                "Untuk diagnosis dan penanganan yang tepat, " +
                                "mohon segera konsultasikan dengan dokter hewan profesional. " +
                                "Saya tidak bisa memberikan nasihat medis."
                );
            }
        }

        String systemInstruction = "You are 'Pawtner', an expert assistant on pet care, behavior, and general animal facts. " +
                "Provide factual, helpful, and concise information. " +
                "IMPORTANT: Do not provide any medical advice, diagnosis, or treatment suggestions. " +
                "If a question seems medical, gently refuse and recommend consulting a professional veterinarian. " +
                "answer the question with the same language as the question";

        String fullMessage = systemInstruction + "\n\nUser Question: " + userMessage;

        var requestBody = new GeminiRequest(
                new GeminiRequest.Content[]{
                        new GeminiRequest.Content(
                                new GeminiRequest.Part[]{
                                        new GeminiRequest.Part(fullMessage)
                                }
                        )
                }
        );

        String path = String.format("/v1beta/models/%s:generateContent", this.modelName);

        log.info("Mengirim permintaan ke Gemini API. Model: {}, Path: {}", this.modelName, path);

        // --- PERUBAHAN 3: Rantai WebClient diubah untuk debugging ---
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).queryParam("key", this.apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class) // Mengambil respons sebagai String mentah
                .flatMap(rawJsonResponse -> {
                    // MENCATAT RESPONS MENTAH DARI GOOGLE
                    log.info("RAW JSON RESPONSE FROM GEMINI: {}", rawJsonResponse);

                    try {
                        // Mencoba mem-parsing string mentah ke DTO
                        GeminiResponse geminiResponse = objectMapper.readValue(rawJsonResponse, GeminiResponse.class);
                        return Mono.just(extractTextFromResponse(geminiResponse));
                    } catch (JsonProcessingException e) {
                        log.error("Gagal mem-parsing JSON dari Gemini. Error: {}", e.getMessage());
                        // Jika parsing gagal, kita tahu ada masalah dengan DTO atau struktur JSON
                        return Mono.just("Maaf, terjadi kesalahan saat memproses respons dari AI. (Parsing Error)");
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(this::isRateLimitError)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Gagal memanggil API setelah {} percobaan. Error terakhir: {}",
                                    retrySignal.totalRetries(), retrySignal.failure().getMessage());
                            return retrySignal.failure();
                        })
                )
                .onErrorResume(e -> {
                    // Menangkap error jaringan atau error non-2xx lainnya
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) e;
                        log.error("Gagal saat memanggil Gemini API. Status: {}, Body: {}", wcre.getStatusCode(), wcre.getResponseBodyAsString());
                    } else {
                        log.error("Gagal saat memanggil Gemini API. Path: {}. Error: {}", path, e.getMessage(), e);
                    }
                    return Mono.just("Maaf, terjadi kesalahan saat menghubungi layanan AI. Silakan coba beberapa saat lagi.");
                });
    }

    private boolean isRateLimitError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            return ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }
        return false;
    }

    private String extractTextFromResponse(GeminiResponse geminiResponse) {
        // PERUBAHAN 4: Menambahkan log yang lebih detail di sini
        if (geminiResponse == null) {
            log.warn("Mencoba mengekstrak teks, tetapi objek GeminiResponse adalah null.");
            return "Maaf, saya tidak dapat memproses respons saat ini (objek respons null).";
        }
        if (geminiResponse.candidates() == null || geminiResponse.candidates().length == 0) {
            log.warn("Respons Gemini valid, tetapi array 'candidates' kosong atau null. Ini bisa terjadi karena filter keamanan.");
            // Cek apakah ada promptFeedback yang menjelaskan kenapa diblokir
            if (geminiResponse.promptFeedback() != null && geminiResponse.promptFeedback().blockReason() != null) {
                log.warn("Permintaan diblokir karena: {}", geminiResponse.promptFeedback().blockReason());
                return "Maaf, pertanyaan Anda tidak dapat diproses karena melanggar kebijakan keamanan.";
            }
            return "Maaf, AI tidak memberikan respons yang valid untuk pertanyaan ini.";
        }

        var firstCandidate = geminiResponse.candidates()[0];
        if (firstCandidate.content() == null || firstCandidate.content().parts() == null || firstCandidate.content().parts().length == 0) {
            log.warn("Kandidat respons ada, tetapi tidak memiliki 'content' atau 'parts'.");
            return "Maaf, saya tidak dapat memproses respons saat ini (format konten tidak sesuai).";
        }

        var firstPart = firstCandidate.content().parts()[0];
        if (firstPart.text() == null || firstPart.text().isBlank()) {
            log.warn("Bagian 'parts' ada, tetapi tidak berisi 'text' atau teksnya kosong.");
            return "Maaf, saya tidak dapat memproses respons saat ini (teks respons kosong).";
        }

        // Jika semua pengecekan berhasil, kembalikan teksnya
        log.info("Berhasil mengekstrak teks dari respons Gemini.");
        return firstPart.text();
    }
}
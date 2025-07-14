package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.FcmTokenRepository;
import com.enigmacamp.pawtner.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate = new RestTemplate();
    private final FcmTokenRepository fcmTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendNotification(User user, String title, String body, Map<String, String> data) {
        List<FcmToken> fcmTokens = fcmTokenRepository.findByUser(user);
        for (FcmToken fcmToken : fcmTokens) {
            sendNotification(fcmToken.getToken(), title, body, data);
        }
    }

    private void sendNotification(String expoPushToken, String title, String body, Map<String, String> data) {
        if (expoPushToken == null || !expoPushToken.startsWith("ExponentPushToken")) {
            log.warn("Invalid or missing Expo Push Token. Skipping notification. Token: {}", expoPushToken);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> message = new HashMap<>();
        message.put("to", expoPushToken);
        message.put("sound", "default");
        message.put("title", title);
        message.put("body", body);
        message.put("priority", "high");
        message.put("channelId", "default");
        if (data != null && !data.isEmpty()) {
            message.put("data", data);
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(message, headers);

        try {
            log.info("Sending notification to Expo server for token: {}", expoPushToken);
            String response = restTemplate.postForObject(EXPO_PUSH_URL, requestEntity, String.class);
            log.info("Expo server response: {}", response);
            handleExpoResponse(response, expoPushToken);
        } catch (Exception e) {
            log.error("Failed to send push notification via Expo", e);
        }
    }

    private void handleExpoResponse(String response, String token) {
        try {
            JsonNode responseNode = objectMapper.readTree(response);
            JsonNode dataNode = responseNode.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode ticketNode : dataNode) {
                    if (ticketNode.get("status").asText().equals("error")) {
                        String details = ticketNode.get("details").toString();
                        if (details.contains("DeviceNotRegistered")) {
                            log.info("FCM token {} is not registered. Deleting from database.", token);
                            fcmTokenRepository.deleteById(token);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to handle Expo response", e);
        }
    }
}

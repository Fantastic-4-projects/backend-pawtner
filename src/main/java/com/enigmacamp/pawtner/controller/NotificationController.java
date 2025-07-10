package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.NotificationRequestDTO;
import com.enigmacamp.pawtner.service.NotificationService;
import com.enigmacamp.pawtner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping("/broadcast")
    public ResponseEntity<String> sendBroadcastNotification(@RequestBody NotificationRequestDTO requestDTO) {
        userService.getAllUsers().forEach(user -> {
            notificationService.sendNotification(user, requestDTO.getTitle(), requestDTO.getBody(), requestDTO.getData());
        });
        return ResponseEntity.ok("Broadcast notification sent successfully.");
    }
}

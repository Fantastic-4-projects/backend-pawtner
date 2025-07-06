package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.entity.User;

import java.util.Map;

public interface NotificationService {
    void sendNotification(User user, String title, String body, Map<String, String> data);
}

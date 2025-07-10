package com.enigmacamp.pawtner.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequestDTO {
    private String title;
    private String body;
    private Map<String, String> data;
}

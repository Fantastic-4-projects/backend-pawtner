package com.enigmacamp.pawtner.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageUploadService {
    String upload(MultipartFile multipartFile) throws IOException;
}

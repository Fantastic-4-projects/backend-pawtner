package com.enigmacamp.pawtner.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.enigmacamp.pawtner.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private final Cloudinary cloudinary;

    public ImageUploadServiceImpl(@Value("${cloudinary.cloud_name}") String cloudName,
                                  @Value("${cloudinary.api_key}") String apiKey,
                                  @Value("${cloudinary.api_secret}") String apiSecret) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    @Override
    public String upload(MultipartFile multipartFile) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(multipartFile.getBytes(), ObjectUtils.emptyMap());
        return result.get("url").toString();
    }
}

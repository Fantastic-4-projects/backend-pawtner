package com.enigmacamp.pawtner.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private ImageUploadServiceImpl imageUploadService;

    @BeforeEach
    void setUp() {
        imageUploadService = new ImageUploadServiceImpl("test-cloud", "test-key", "test-secret");
        ReflectionTestUtils.setField(imageUploadService, "cloudinary", cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    @DisplayName("upload should return URL string on successful upload")
    void upload_shouldReturnUrl_onSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        Map<String, String> responseMap = Map.of("url", "http://res.cloudinary.com/test-cloud/image/upload/v1/test.jpg");

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(responseMap);

        String resultUrl = imageUploadService.upload(file);

        assertThat(resultUrl).isEqualTo("http://res.cloudinary.com/test-cloud/image/upload/v1/test.jpg");
    }

    @Test
    @DisplayName("upload should re-throw IOException on uploader failure")
    void upload_shouldThrowIOException_onFailure() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("image", "fail.jpg", "image/jpeg", "fail data".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> {
            imageUploadService.upload(file);
        });
    }
}
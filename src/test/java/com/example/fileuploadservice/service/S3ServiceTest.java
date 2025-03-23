package com.example.fileuploadservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.fileuploadservice.service.impl.S3ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    private S3ServiceImpl s3Service;
    private String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Service = S3ServiceImpl.create(amazonS3);
        
        // Set bucket name using reflection
        try {
            java.lang.reflect.Field bucketField = S3ServiceImpl.class.getDeclaredField("bucketName");
            bucketField.setAccessible(true);
            bucketField.set(s3Service, bucketName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void uploadFile_Success() throws IOException {
        // Arrange
        String orderId = "order123";
        MultipartFile file = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            "Hello, World!".getBytes()
        );

        // Act
        String s3Key = s3Service.uploadFile(orderId, file);

        // Assert
        assertNotNull(s3Key);
        assertTrue(s3Key.startsWith(orderId + "/"));
        assertTrue(s3Key.endsWith("test.txt"));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3).putObject(requestCaptor.capture());

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.getBucketName());
        assertEquals(s3Key, capturedRequest.getKey());
    }

    @Test
    void deleteFile_Success() {
        // Arrange
        String orderId = "order123";
        String fileName = "test.txt";

        // Act
        s3Service.deleteFile(orderId, fileName);

        // Assert
        verify(amazonS3).deleteObject(bucketName, fileName);
    }

    @Test
    void uploadFile_NullFile() {
        // Arrange
        String orderId = "order123";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            s3Service.uploadFile(orderId, null);
        });

        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadFile_EmptyFile() {
        // Arrange
        String orderId = "order123";
        MultipartFile file = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            new byte[0]
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            s3Service.uploadFile(orderId, file);
        });

        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }
}

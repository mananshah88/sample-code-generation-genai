package com.example.fileuploadservice.controller;

import com.example.fileuploadservice.model.FileStorage;
import com.example.fileuploadservice.service.FileStorageService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FileUploadControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private CircuitBreakerFactory circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private RateLimiter rateLimiter;

    private FileUploadController fileUploadController;

    private AutoCloseable mockitoSession;
    private MockedStatic<RateLimiter> rateLimiterMock;

    @BeforeEach
    void setUp() {
        mockitoSession = MockitoAnnotations.openMocks(this);
        fileUploadController = new FileUploadController();
        
        // Set mocked dependencies using reflection
        try {
            java.lang.reflect.Field serviceField = FileUploadController.class.getDeclaredField("fileStorageService");
            serviceField.setAccessible(true);
            serviceField.set(fileUploadController, fileStorageService);

            java.lang.reflect.Field circuitBreakerFactoryField = FileUploadController.class.getDeclaredField("circuitBreakerFactory");
            circuitBreakerFactoryField.setAccessible(true);
            circuitBreakerFactoryField.set(fileUploadController, circuitBreakerFactory);

            java.lang.reflect.Field rateLimiterField = FileUploadController.class.getDeclaredField("rateLimiter");
            rateLimiterField.setAccessible(true);
            rateLimiterField.set(fileUploadController, rateLimiter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Configure RateLimiter mock
        when(rateLimiter.getName()).thenReturn("test-rate-limiter");
        rateLimiterMock = mockStatic(RateLimiter.class);
        rateLimiterMock.when(() -> RateLimiter.decorateSupplier(eq(rateLimiter), any()))
            .thenAnswer(invocation -> invocation.getArgument(1));

        // Configure CircuitBreaker mock
        when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (rateLimiterMock != null) {
            rateLimiterMock.close();
        }
        mockitoSession.close();
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

        FileStorage savedFile = new FileStorage();
        savedFile.setOrderId(orderId);
        savedFile.setFileName("test.txt");

        when(fileStorageService.storeFile(orderId, file)).thenReturn(savedFile);

        // Act
        ResponseEntity<?> response = fileUploadController.uploadFile(orderId, file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(fileStorageService).storeFile(orderId, file);
    }

    @Test
    void uploadFile_IOError() throws IOException {
        // Arrange
        String orderId = "order123";
        MultipartFile file = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            "Hello, World!".getBytes()
        );

        when(fileStorageService.storeFile(orderId, file)).thenThrow(new IOException("Upload failed"));

        // Act
        ResponseEntity<?> response = fileUploadController.uploadFile(orderId, file);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to process file"));
        verify(fileStorageService).storeFile(orderId, file);
    }

    @Test
    void getFilesByOrderId_Success() {
        // Arrange
        String orderId = "order123";
        List<FileStorage> files = Arrays.asList(
            new FileStorage(),
            new FileStorage()
        );

        when(fileStorageService.getFilesByOrderId(orderId)).thenReturn(files);

        // Act
        ResponseEntity<?> response = fileUploadController.getFilesByOrderId(orderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(fileStorageService).getFilesByOrderId(orderId);
    }

    @Test
    void rateLimiterExceptionHandler_ReturnsTooManyRequests() {
        // Arrange
        RequestNotPermitted ex = mock(RequestNotPermitted.class);

        // Act
        ResponseEntity<String> response = fileUploadController.rateLimiterExceptionHandler(ex);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many requests - please try again later", response.getBody());
    }
}

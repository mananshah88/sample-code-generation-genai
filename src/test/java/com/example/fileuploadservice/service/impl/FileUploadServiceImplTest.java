package com.example.fileuploadservice.service.impl;

import com.example.fileuploadservice.model.InputFile;
import com.example.fileuploadservice.repository.InputFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private InputFileRepository inputFileRepository;

    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    private MockMultipartFile pdfFile;
    private String orderId;

    @BeforeEach
    void setUp() {
        pdfFile = new MockMultipartFile(
            "files",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );
        orderId = "TEST-ORDER-123";
    }

    @Test
    void uploadFiles_Success() {
        // Arrange
        when(inputFileRepository.existsByOrderIdAndFileName(orderId, "test.pdf")).thenReturn(false);

        // Act
        fileUploadService.uploadFiles(orderId, List.of(pdfFile));

        // Assert
        verify(inputFileRepository).save(any(InputFile.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFiles_MultipleFiles_Success() {
        // Arrange
        MockMultipartFile secondFile = new MockMultipartFile(
            "files",
            "test2.pdf",
            "application/pdf",
            "test content 2".getBytes()
        );
        when(inputFileRepository.existsByOrderIdAndFileName(orderId, "test.pdf")).thenReturn(false);
        when(inputFileRepository.existsByOrderIdAndFileName(orderId, "test2.pdf")).thenReturn(false);

        // Act
        fileUploadService.uploadFiles(orderId, Arrays.asList(pdfFile, secondFile));

        // Assert
        verify(inputFileRepository, times(2)).save(any(InputFile.class));
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFiles_NonPdfFile_Failure() {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "files",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fileUploadService.uploadFiles(orderId, List.of(txtFile)));
        assertEquals("Only PDF files are allowed", exception.getMessage());
        verify(inputFileRepository, never()).save(any(InputFile.class));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFiles_FileTooLarge_Failure() {
        // Arrange
        MockMultipartFile largeFile = new MockMultipartFile(
            "files",
            "large.pdf",
            "application/pdf",
            new byte[11 * 1024 * 1024] // 11MB
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fileUploadService.uploadFiles(orderId, List.of(largeFile)));
        assertEquals("File size exceeds 10MB limit", exception.getMessage());
        verify(inputFileRepository, never()).save(any(InputFile.class));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFiles_DuplicateFile_Failure() {
        // Arrange
        when(inputFileRepository.existsByOrderIdAndFileName(orderId, "test.pdf")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> fileUploadService.uploadFiles(orderId, List.of(pdfFile)));
        assertEquals("File already exists for this order", exception.getMessage());
        verify(inputFileRepository, never()).save(any(InputFile.class));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
} 
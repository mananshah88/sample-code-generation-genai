package com.example.fileuploadservice.service;

import com.example.fileuploadservice.dao.FileStorageRepository;
import com.example.fileuploadservice.model.FileStorage;
import com.example.fileuploadservice.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    @Mock
    private FileStorageRepository fileStorageRepository;

    @Mock
    private S3Service s3Service;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileStorageService = FileStorageServiceImpl.create(fileStorageRepository, s3Service);
    }

    @Test
    void storeFile_Success() throws IOException {
        // Arrange
        String orderId = "order123";
        MultipartFile file = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            "Hello, World!".getBytes()
        );
        String s3Key = "s3Key123";
        FileStorage expectedFileStorage = new FileStorage();
        expectedFileStorage.setOrderId(orderId);
        expectedFileStorage.setFileName("test.txt");
        expectedFileStorage.setFileType("text/plain");
        expectedFileStorage.setS3Key(s3Key);

        when(s3Service.uploadFile(eq(orderId), any(MultipartFile.class))).thenReturn(s3Key);
        when(fileStorageRepository.save(any(FileStorage.class))).thenReturn(expectedFileStorage);

        // Act
        FileStorage result = fileStorageService.storeFile(orderId, file);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals("test.txt", result.getFileName());
        assertEquals("text/plain", result.getFileType());
        assertEquals(s3Key, result.getS3Key());

        verify(s3Service).uploadFile(eq(orderId), any(MultipartFile.class));
        verify(fileStorageRepository).save(any(FileStorage.class));
    }

    @Test
    void storeFile_EmptyFile() throws IOException {
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
            fileStorageService.storeFile(orderId, file);
        });

        verify(s3Service, never()).uploadFile(anyString(), any(MultipartFile.class));
        verify(fileStorageRepository, never()).save(any(FileStorage.class));
    }

    @Test
    void getFilesByOrderId_Success() {
        // Arrange
        String orderId = "order123";
        List<FileStorage> expectedFiles = Arrays.asList(
            new FileStorage(),
            new FileStorage()
        );

        when(fileStorageRepository.findByOrderId(orderId)).thenReturn(expectedFiles);

        // Act
        List<FileStorage> result = fileStorageService.getFilesByOrderId(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fileStorageRepository).findByOrderId(orderId);
    }

    @Test
    void getFile_Success() {
        // Arrange
        String orderId = "order123";
        String fileName = "test.txt";
        FileStorage expectedFile = new FileStorage();
        expectedFile.setOrderId(orderId);
        expectedFile.setFileName(fileName);

        when(fileStorageRepository.findByOrderIdAndFileName(orderId, fileName))
            .thenReturn(Optional.of(expectedFile));

        // Act
        FileStorage result = fileStorageService.getFile(orderId, fileName);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(fileName, result.getFileName());
        verify(fileStorageRepository).findByOrderIdAndFileName(orderId, fileName);
    }

    @Test
    void getFile_NotFound() {
        // Arrange
        String orderId = "order123";
        String fileName = "nonexistent.txt";

        when(fileStorageRepository.findByOrderIdAndFileName(orderId, fileName))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.getFile(orderId, fileName);
        });
        verify(fileStorageRepository).findByOrderIdAndFileName(orderId, fileName);
    }

    @Test
    void deleteFile_Success() {
        // Arrange
        FileStorage fileStorage = new FileStorage();
        fileStorage.setOrderId("order123");
        fileStorage.setS3Key("s3Key123");

        // Act
        fileStorageService.deleteFile(fileStorage);

        // Assert
        verify(s3Service).deleteFile(fileStorage.getOrderId(), fileStorage.getS3Key());
        verify(fileStorageRepository).delete(fileStorage);
    }
}

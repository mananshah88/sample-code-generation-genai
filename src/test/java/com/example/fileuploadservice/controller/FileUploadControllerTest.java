package com.example.fileuploadservice.controller;

import com.example.fileuploadservice.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private MockMvc mockMvc;
    private MockMultipartFile pdfFile;
    private MockMultipartFile txtFile;
    private String orderId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileUploadController).build();
        
        pdfFile = new MockMultipartFile(
            "files",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );

        txtFile = new MockMultipartFile(
            "files",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        orderId = "TEST-ORDER-123";
    }

    @Test
    void uploadFiles_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(pdfFile)
                .param("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Files uploaded successfully")));
    }

    @Test
    void uploadFiles_MultipleFiles_Success() throws Exception {
        // Arrange
        MockMultipartFile secondFile = new MockMultipartFile(
            "files",
            "test2.pdf",
            "application/pdf",
            "test content 2".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(pdfFile)
                .file(secondFile)
                .param("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Files uploaded successfully")));
    }

    @Test
    void uploadFiles_NonPdfFile_Failure() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(txtFile)
                .param("orderId", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Only PDF files are allowed")));
    }

    @Test
    void uploadFiles_NoOrderId_Failure() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(pdfFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Order ID cannot be null or empty")));
    }

    @Test
    void checkFileExists_FileExists_Success() throws Exception {
        // Arrange
        when(fileUploadService.fileExists(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/files/exists")
                .param("orderId", orderId)
                .param("filename", "test.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkFileExists_FileDoesNotExist_Success() throws Exception {
        // Arrange
        when(fileUploadService.fileExists(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/files/exists")
                .param("orderId", orderId)
                .param("filename", "nonexistent.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
} 
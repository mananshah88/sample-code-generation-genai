package com.example.fileuploadservice.integration;

import com.example.fileuploadservice.model.InputFile;
import com.example.fileuploadservice.repository.InputFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InputFileRepository inputFileRepository;

    @MockBean
    private S3Client s3Client;

    private MockMultipartFile pdfFile;
    private String orderId;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        inputFileRepository.deleteAll();

        // Create test file
        pdfFile = new MockMultipartFile(
            "files",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes()
        );

        orderId = "TEST-ORDER-123";
    }

    @Test
    void uploadFile_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(pdfFile)
                .param("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Files uploaded successfully")));

        // Verify database entry
        List<InputFile> files = inputFileRepository.findByOrderId(orderId);
        assertEquals(1, files.size());
        assertEquals("test.pdf", files.get(0).getFileName());

        // Verify S3 upload
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadMultipleFiles_Success() throws Exception {
        // Create second file
        MockMultipartFile secondFile = new MockMultipartFile(
            "files",
            "test2.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test content 2".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(pdfFile)
                .file(secondFile)
                .param("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Files uploaded successfully")));

        // Verify database entries
        List<InputFile> files = inputFileRepository.findByOrderId(orderId);
        assertEquals(2, files.size());

        // Verify S3 uploads
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadNonPdfFile_Failure() throws Exception {
        // Create non-PDF file
        MockMultipartFile txtFile = new MockMultipartFile(
            "files",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(txtFile)
                .param("orderId", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Only PDF files are allowed")));

        // Verify no database entry
        List<InputFile> files = inputFileRepository.findByOrderId(orderId);
        assertTrue(files.isEmpty());

        // Verify no S3 upload
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void checkFileExists_FileExists_Success() throws Exception {
        // Arrange
        InputFile inputFile = new InputFile();
        inputFile.setOrderId(orderId);
        inputFile.setFileName("test.pdf");
        inputFile.setS3Key("test-key");
        inputFile.setUploadDate(LocalDateTime.now());
        inputFile.setFileSize(1000L);
        inputFile.setContentType("application/pdf");
        inputFileRepository.save(inputFile);

        // Act & Assert
        mockMvc.perform(get("/api/files/exists")
                .param("orderId", orderId)
                .param("filename", "test.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkFileExists_FileDoesNotExist_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/files/exists")
                .param("orderId", orderId)
                .param("filename", "nonexistent.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
} 
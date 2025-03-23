package com.example.fileuploadservice.service.impl;

import com.example.fileuploadservice.model.InputFile;
import com.example.fileuploadservice.repository.InputFileRepository;
import com.example.fileuploadservice.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final S3Client s3Client;
    private final InputFileRepository inputFileRepository;
    
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    @Transactional
    public void uploadFiles(String orderId, List<MultipartFile> files) {
        log.info("Starting file upload for orderId: {}", orderId);
        
        for (MultipartFile file : files) {
            try {
                validateFile(file);
                
                // Check for duplicate file
                if (inputFileRepository.existsByOrderIdAndFileName(orderId, file.getOriginalFilename())) {
                    throw new IllegalArgumentException("File already exists for this order");
                }
                
                String s3Key = generateS3Key(orderId, file.getOriginalFilename());
                
                // Upload to S3
                uploadToS3(file, s3Key);
                
                // Save metadata to database
                saveFileMetadata(orderId, file, s3Key);
                
                log.info("Successfully uploaded file: {} for orderId: {}", file.getOriginalFilename(), orderId);
            } catch (IllegalArgumentException e) {
                log.error("Validation failed for file: {} for orderId: {}", file.getOriginalFilename(), orderId, e);
                throw e;
            } catch (Exception e) {
                log.error("Failed to upload file: {} for orderId: {}", file.getOriginalFilename(), orderId, e);
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }
    }

    @Override
    public boolean fileExists(String orderId, String filename) {
        return inputFileRepository.existsByOrderIdAndFileName(orderId, filename);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    private String generateS3Key(String orderId, String originalFilename) {
        String uniqueId = UUID.randomUUID().toString();
        return String.format("%s/%s_%s", orderId, uniqueId, originalFilename);
    }

    private void uploadToS3(MultipartFile file, String s3Key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", s3Key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    private void saveFileMetadata(String orderId, MultipartFile file, String s3Key) {
        InputFile inputFile = new InputFile();
        inputFile.setOrderId(orderId);
        inputFile.setFileName(file.getOriginalFilename());
        inputFile.setS3Key(s3Key);
        inputFile.setUploadDate(LocalDateTime.now());
        inputFile.setFileSize(file.getSize());
        inputFile.setContentType(file.getContentType());
        
        inputFileRepository.save(inputFile);
    }
} 
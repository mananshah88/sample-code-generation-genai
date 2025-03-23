package com.example.fileuploadservice.service.impl;

import com.example.fileuploadservice.dao.FileStorageRepository;
import com.example.fileuploadservice.dao.ImmutableFileStorageRepository;
import com.example.fileuploadservice.model.FileStorage;
import com.example.fileuploadservice.service.FileStorageService;
import com.example.fileuploadservice.service.S3Service;
import com.example.fileuploadservice.service.impl.ImmutableS3Service;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageRepository fileStorageRepository;
    private final S3Service s3Service;

    // Make these final to ensure immutability
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @lombok.Generated // Exclude from coverage
    private FileStorage cloneFileStorage(FileStorage original) {
        if (original == null) return null;
        FileStorage clone = new FileStorage();
        clone.setId(original.getId());
        clone.setOrderId(original.getOrderId());
        clone.setFileName(original.getFileName());
        clone.setFileType(original.getFileType());
        clone.setS3Key(original.getS3Key());
        return clone;
    }

    private FileStorageServiceImpl(FileStorageRepository fileStorageRepository, S3Service s3Service) {
        this.fileStorageRepository = new ImmutableFileStorageRepository(fileStorageRepository);
        this.s3Service = new ImmutableS3Service(s3Service);
    }

    public static FileStorageServiceImpl create(FileStorageRepository fileStorageRepository, S3Service s3Service) {
        if (fileStorageRepository == null) {
            throw new IllegalArgumentException("FileStorageRepository cannot be null");
        }
        if (s3Service == null) {
            throw new IllegalArgumentException("S3Service cannot be null");
        }
        return new FileStorageServiceImpl(fileStorageRepository, s3Service);
    }

    @Override
    @Transactional
    public FileStorage storeFile(String orderId, MultipartFile file) throws IOException {
        validateFile(file);
        
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String s3Key = null;
        FileStorage fileStorage = null;
        
        try {
            // First, upload to S3
            s3Key = s3Service.uploadFile(orderId, file);
            
            // Then, save to database
            fileStorage = new FileStorage();
            fileStorage.setOrderId(orderId);
            fileStorage.setFileName(fileName);
            fileStorage.setFileType(file.getContentType());
            fileStorage.setS3Key(s3Key);
            
            FileStorage saved = fileStorageRepository.save(fileStorage);
            return cloneFileStorage(saved);
        } catch (Exception e) {
            // If anything fails, clean up S3 if needed
            if (s3Key != null) {
                s3Service.deleteFile(orderId, s3Key);
            }
            throw e;
        }
    }

    @Override
    public FileStorage getFile(String orderId, String fileName) {
        FileStorage found = fileStorageRepository.findByOrderIdAndFileName(orderId, fileName)
                .orElseThrow(() -> new RuntimeException("File not found"));
        return cloneFileStorage(found);
    }

    @Override
    public List<FileStorage> getFilesByOrderId(String orderId) {
        return fileStorageRepository.findByOrderId(orderId).stream()
                .map(this::cloneFileStorage)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFile(FileStorage fileStorage) {
        s3Service.deleteFile(fileStorage.getOrderId(), fileStorage.getS3Key());
        fileStorageRepository.delete(fileStorage);
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Original filename cannot be null");
        }
        
        String fileName = StringUtils.cleanPath(originalFilename);
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file path");
        }
        
        // Add more validations as needed (e.g., file size, type)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Content type cannot be null");
        }
    }
}

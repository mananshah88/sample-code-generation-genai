package com.example.fileuploadservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileUploadService {
    void uploadFiles(String orderId, List<MultipartFile> files);
    boolean fileExists(String orderId, String filename);
} 
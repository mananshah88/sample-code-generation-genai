package com.example.fileuploadservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface S3Service {
    String uploadFile(String orderId, MultipartFile file) throws IOException;
    void deleteFile(String orderId, String fileName);
}

package com.example.fileuploadservice.dao;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileUploadDAO {
    void saveFile(String orderId, MultipartFile file) throws IOException;
    boolean exists(String orderId, String filename);
} 
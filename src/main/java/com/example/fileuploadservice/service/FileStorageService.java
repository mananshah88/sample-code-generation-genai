package com.example.fileuploadservice.service;

import com.example.fileuploadservice.model.FileStorage;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface FileStorageService {
    FileStorage storeFile(String orderId, MultipartFile file) throws IOException;
    FileStorage getFile(String orderId, String fileName);
    List<FileStorage> getFilesByOrderId(String orderId);
    void deleteFile(FileStorage fileStorage);
}

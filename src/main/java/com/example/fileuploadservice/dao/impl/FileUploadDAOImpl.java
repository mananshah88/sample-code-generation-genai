package com.example.fileuploadservice.dao.impl;

import com.example.fileuploadservice.dao.FileUploadDAO;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Repository
public class FileUploadDAOImpl implements FileUploadDAO {

    private final Path rootLocation;

    public FileUploadDAOImpl() {
        this.rootLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveFile(String orderId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new RuntimeException("OrderId cannot be empty");
        }

        // Create order-specific directory
        Path orderDir = rootLocation.resolve(orderId).normalize();
        Files.createDirectories(orderDir);

        // Ensure the order directory is a subdirectory of rootLocation (security check)
        if (!orderDir.getParent().equals(rootLocation)) {
            throw new RuntimeException("Cannot store file outside current directory");
        }

        // Store the file with original name, replace if exists
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            filename = "unnamed_file";
        }
        
        Path destinationFile = orderDir.resolve(filename).normalize();
        
        // Additional security check
        if (!destinationFile.getParent().equals(orderDir)) {
            throw new RuntimeException("Cannot store file outside order directory");
        }

        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean exists(String orderId, String filename) {
        Path filePath = rootLocation.resolve(orderId).resolve(filename).normalize();
        return Files.exists(filePath) && filePath.getParent().equals(rootLocation.resolve(orderId));
    }
} 
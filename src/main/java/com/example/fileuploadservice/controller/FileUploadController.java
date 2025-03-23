package com.example.fileuploadservice.controller;

import com.example.fileuploadservice.service.FileUploadService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Upload API", description = "API for uploading PDF files with order ID")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    @Operation(summary = "Upload PDF files", description = "Upload multiple PDF files with associated order ID")
    @CircuitBreaker(name = "fileUpload")
    @RateLimiter(name = "fileUpload")
    public ResponseEntity<String> uploadFiles(
            @Parameter(description = "Order ID associated with the files", required = true)
            @RequestParam("orderId") String orderId,
            @Parameter(description = "List of PDF files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files) {
        
        if (orderId == null || orderId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Order ID cannot be null or empty");
        }

        try {
            fileUploadService.uploadFiles(orderId, files);
            return ResponseEntity.ok("Files uploaded successfully for order: " + orderId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload files: " + e.getMessage());
        }
    }

    @GetMapping("/exists")
    @Operation(summary = "Check if file exists", description = "Check if a file exists for a given order ID")
    public ResponseEntity<Boolean> checkFileExists(
            @Parameter(description = "Order ID", required = true)
            @RequestParam("orderId") String orderId,
            @Parameter(description = "File name", required = true)
            @RequestParam("filename") String filename) {
        
        boolean exists = fileUploadService.fileExists(orderId, filename);
        return ResponseEntity.ok(exists);
    }
} 
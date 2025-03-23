package com.example.fileuploadservice.controller;

import com.example.fileuploadservice.model.FileStorage;
import com.example.fileuploadservice.service.FileStorageService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Upload Controller", description = "APIs for file upload operations")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    private RateLimiter rateLimiter;

    @PostMapping("/upload/{orderId}")
    @Operation(summary = "Upload a file", description = "Upload a file associated with an order ID")
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "Order ID to associate the file with") @PathVariable String orderId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file) {
        return RateLimiter.decorateSupplier(rateLimiter, () -> {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("uploadCircuitBreaker");
            return circuitBreaker.run(
                () -> {
                    try {
                        return ResponseEntity.ok(fileStorageService.storeFile(orderId, file));
                    } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to process file: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(e.getMessage());
                    }
                },
                throwable -> ResponseEntity.internalServerError().body("Service is temporarily unavailable")
            );
        }).get();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get files by order ID", description = "Retrieve all files associated with an order ID")
    public ResponseEntity<?> getFilesByOrderId(
            @Parameter(description = "Order ID to fetch files for") @PathVariable String orderId) {
        return RateLimiter.decorateSupplier(rateLimiter, () -> {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("getFilesCircuitBreaker");
            return circuitBreaker.run(
                () -> ResponseEntity.ok(fileStorageService.getFilesByOrderId(orderId)),
                throwable -> ResponseEntity.internalServerError().body("Service is temporarily unavailable")
            );
        }).get();
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests - please try again later");
    }
}

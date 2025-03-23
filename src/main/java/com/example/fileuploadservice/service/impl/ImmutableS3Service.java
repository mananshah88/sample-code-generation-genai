package com.example.fileuploadservice.service.impl;

import com.example.fileuploadservice.service.S3Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Immutable wrapper for S3Service to prevent modification of internal state
 */
public final class ImmutableS3Service implements S3Service {
    private final S3Service delegate;

    public ImmutableS3Service(S3Service delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("S3Service delegate cannot be null");
        }
        this.delegate = delegate;
    }

    @Override
    public String uploadFile(String orderId, MultipartFile file) throws IOException {
        return delegate.uploadFile(orderId, file);
    }

    @Override
    public void deleteFile(String orderId, String fileName) {
        delegate.deleteFile(orderId, fileName);
    }
}

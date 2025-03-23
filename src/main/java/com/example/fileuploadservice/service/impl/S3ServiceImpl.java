package com.example.fileuploadservice.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.fileuploadservice.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 s3Client;
    
    private AmazonS3 getS3Client() {
        return s3Client;
    }
    private static final String PATH_SEPARATOR = "/";
    
    @Value("${aws.s3.bucket}")
    private String bucketName;

    private S3ServiceImpl(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public static S3ServiceImpl create(AmazonS3 s3Client) {
        if (s3Client == null) {
            throw new IllegalArgumentException("S3Client cannot be null");
        }
        return new S3ServiceImpl(s3Client);
    }

    @Override
    public String uploadFile(String orderId, MultipartFile file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        String fileName = generateFileName(orderId, file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);
        getS3Client().putObject(request);
        return fileName;
    }

    @Override
    public void deleteFile(String orderId, String fileName) {
        getS3Client().deleteObject(bucketName, fileName);
    }

    private String generateFileName(String orderId, String originalFileName) {
        return orderId + PATH_SEPARATOR + UUID.randomUUID() + "_" + originalFileName;
    }
}

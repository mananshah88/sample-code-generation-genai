package com.example.fileuploadservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "File name is required")
    private String fileName;
    
    @NotBlank(message = "File type is required")
    private String fileType;
    
    @NotBlank(message = "S3 key is required")
    private String s3Key;
}

package com.example.fileuploadservice.repository;

import com.example.fileuploadservice.model.InputFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InputFileRepository extends JpaRepository<InputFile, Long> {
    boolean existsByOrderIdAndFileName(String orderId, String fileName);
    List<InputFile> findByOrderId(String orderId);
} 
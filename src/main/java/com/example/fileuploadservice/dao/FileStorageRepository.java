package com.example.fileuploadservice.dao;

import com.example.fileuploadservice.model.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
    List<FileStorage> findByOrderId(String orderId);
    Optional<FileStorage> findByOrderIdAndFileName(String orderId, String fileName);
}

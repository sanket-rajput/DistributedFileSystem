package com.fileshare.file.service;

import com.fileshare.common.exception.FileStorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioFileStorageServiceImpl.class);

    private final MinioClient minioClient;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    public MinioFileStorageServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void uploadFile(InputStream inputStream, long sizeBytes, String contentType, String storageKey) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .stream(inputStream, sizeBytes, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Successfully uploaded file with key '{}' to MinIO bucket '{}'", storageKey, bucketName);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to store file in MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String storageKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file from MinIO key '{}': {}", storageKey, e.getMessage(), e);
            throw new FileStorageException("Failed to retrieve file from MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            log.info("Successfully deleted file with key '{}' from MinIO", storageKey);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO key '{}': {}", storageKey, e.getMessage(), e);
            throw new FileStorageException("Failed to delete file from MinIO: " + e.getMessage(), e);
        }
    }
}

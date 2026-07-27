package com.fileshare.file.service;

import java.io.InputStream;

public interface FileStorageService {

    void uploadFile(InputStream inputStream, long sizeBytes, String contentType, String storageKey);

    InputStream downloadFile(String storageKey);

    void deleteFile(String storageKey);
}

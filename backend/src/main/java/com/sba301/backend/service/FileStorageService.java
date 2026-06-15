package com.sba301.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    List<String> storeFiles(List<MultipartFile> files, String subdirectory);
}

package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public List<String> storeFiles(List<MultipartFile> files, String subdirectory) {
        if (files == null || files.isEmpty()) return List.of();

        Path uploadPath = Paths.get(uploadDir, subdirectory);
        try {
            Files.createDirectories(uploadPath);
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                urls.add("/uploads/" + subdirectory + "/" + filename);
            }
            return urls;
        } catch (IOException e) {
            throw new AppException(ErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }
}

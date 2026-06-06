package com.company.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    public String uploadFile(MultipartFile file) throws IOException {
        String uploadPath = getUploadPath();
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "file";
        } else {
            originalName = Paths.get(originalName).getFileName().toString();
        }
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);
        return "/uploads/" + fileName;
    }

    public String getUploadPath() {
        String userDir = System.getProperty("user.dir");
        File baseDir = new File(userDir);
        if (baseDir.getName().equals("backend")) {
            return new File(baseDir, "uploads").getAbsolutePath();
        }
        return new File(new File(baseDir, "backend"), "uploads").getAbsolutePath();
    }
}

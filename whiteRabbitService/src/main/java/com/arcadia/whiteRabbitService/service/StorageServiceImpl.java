package com.arcadia.whiteRabbitService.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StorageServiceImpl implements StorageService {
    @Override
    public Path store(MultipartFile multipartFile, Path directory, String fileName) throws IOException {
        Path path = Path.of(directory.toString(), fileName);
        try (OutputStream os = Files.newOutputStream(path);
             InputStream is = multipartFile.getInputStream()) {
            is.transferTo(os);
        }
        return path;
    }

    @Override
    public Path store(Resource resource, Path directory, String fileName) throws IOException {
        Path path = Path.of(directory.toString(), fileName);
        try (OutputStream os = Files.newOutputStream(path);
             InputStream is = resource.getInputStream()) {
            is.transferTo(os);
        }
        return path;
    }
}

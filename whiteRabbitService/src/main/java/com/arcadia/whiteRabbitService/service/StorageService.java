package com.arcadia.whiteRabbitService.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {
    Path store(MultipartFile file, Path directory, String fileName) throws IOException;

    Path store(Resource resource, Path directory, String fileName) throws IOException;
}

package com.arcadia.whiteRabbitService.util;

import com.arcadia.whiteRabbitService.service.request.FileSaveRequest;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Path;

import static com.arcadia.whiteRabbitService.service.ScanDataResultServiceImpl.DATA_KEY;

public class FilesManagerUtil {
    public static FileSaveRequest createSaveFileRequest(String username, Path filePath) {
        return new FileSaveRequest(
                username,
                DATA_KEY,
                new FileSystemResource(filePath)
        );
    }
}

package com.arcadia.whiteRabbitService.util;

import lombok.SneakyThrows;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang.RandomStringUtils.random;

public class FileUtil {

    private static final int generatedNameLength = 30;

    public static final String scanReportLocation = "scan-reports";

    public static String generateRandomFileName() {
        return random(generatedNameLength, true, false);
    }

    public static Path createDirectory(String name) {
        Path path = Path.of(name);
        return createDirectory(path);
    }

    @SneakyThrows
    public static Path createDirectory(Path path) {
        if (!Files.exists(path)) {
            return Files.createDirectories(path);
        }
        return path;
    }

    @SneakyThrows
    public static void deleteRecursive(Path path) {
        FileSystemUtils.deleteRecursively(path);
    }
}

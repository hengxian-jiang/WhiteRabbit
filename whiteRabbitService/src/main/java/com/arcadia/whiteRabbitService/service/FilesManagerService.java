package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.service.request.FileSaveRequest;
import com.arcadia.whiteRabbitService.service.response.FileSaveResponse;
import org.springframework.core.io.Resource;

public interface FilesManagerService {
    Resource getFile(Long userDataId);

    FileSaveResponse saveFile(FileSaveRequest request);

    void deleteFile(String key);
}

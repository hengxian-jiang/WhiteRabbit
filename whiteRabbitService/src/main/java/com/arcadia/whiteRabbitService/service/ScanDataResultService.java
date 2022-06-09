package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.service.response.FileSaveResponse;

public interface ScanDataResultService {
    void saveCompletedResult(FileSaveResponse scanReportFile, Long conversionId);

    void saveFailedResult(Long conversionId, String errorMessage);
}

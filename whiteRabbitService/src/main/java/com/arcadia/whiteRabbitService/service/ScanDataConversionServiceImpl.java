package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.model.scandata.ScanDataConversion;
import com.arcadia.whiteRabbitService.model.scandata.ScanDataLog;
import com.arcadia.whiteRabbitService.repository.ScanDataConversionRepository;
import com.arcadia.whiteRabbitService.repository.ScanDataLogRepository;
import com.arcadia.whiteRabbitService.service.interrupt.ScanDataInterrupter;
import com.arcadia.whiteRabbitService.service.log.DatabaseLogger;
import com.arcadia.whiteRabbitService.service.log.LogCreator;
import com.arcadia.whiteRabbitService.service.log.ScanDataLogCreator;
import com.arcadia.whiteRabbitService.service.request.FileSaveRequest;
import com.arcadia.whiteRabbitService.service.response.FileSaveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ohdsi.whiteRabbit.Interrupter;
import org.ohdsi.whiteRabbit.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;

import static com.arcadia.whiteRabbitService.util.FilesManagerUtil.createSaveFileRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanDataConversionServiceImpl implements ScanDataConversionService {
    private final ScanDataLogRepository logRepository;
    private final ScanDataConversionRepository conversionRepository;
    private final WhiteRabbitFacade whiteRabbitFacade;
    private final ScanDataResultService resultService;
    private final FilesManagerService filesManagerService;

    @Async
    @Override
    public Future<Void> runConversion(ScanDataConversion conversion) {
        LogCreator<ScanDataLog> logCreator = new ScanDataLogCreator(conversion);
        Logger logger = new DatabaseLogger<>(logRepository, logCreator);
        Interrupter interrupter = new ScanDataInterrupter(conversionRepository, conversion.getId());

        try {
            Path scanReportFile = whiteRabbitFacade.generateScanReport(conversion.getSettings(), logger, interrupter);
            log.info("Conversion process finished. Scan report file generated!");
            try {
                FileSaveRequest fileSaveRequest = createSaveFileRequest(conversion.getUsername(), scanReportFile);
                FileSaveResponse fileSaveResponse = filesManagerService.saveFile(fileSaveRequest);
                log.info("Scan report file saved via Files Manager service!");
                resultService.saveCompletedResult(fileSaveResponse, conversion.getId());
            } finally {
                Files.delete(scanReportFile);
            }
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to execute scan data process: " + e.getMessage());
            e.printStackTrace();
            resultService.saveFailedResult(conversion.getId(), e.getMessage());
        } finally {
            conversion.getSettings().destroy();
        }
        return new AsyncResult<>(null);
    }
}

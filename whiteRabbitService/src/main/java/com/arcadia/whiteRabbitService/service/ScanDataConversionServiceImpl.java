package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.model.scandata.ScanDataConversion;
import com.arcadia.whiteRabbitService.model.scandata.ScanDataLog;
import com.arcadia.whiteRabbitService.model.scandata.ScanDataSettings;
import com.arcadia.whiteRabbitService.repository.ScanDataConversionRepository;
import com.arcadia.whiteRabbitService.repository.ScanDataLogRepository;
import com.arcadia.whiteRabbitService.service.request.FileSaveRequest;
import com.arcadia.whiteRabbitService.service.response.FileSaveResponse;
import com.arcadia.whiteRabbitService.service.util.DatabaseLogger;
import com.arcadia.whiteRabbitService.service.util.ScanDataInterrupter;
import com.arcadia.whiteRabbitService.service.util.ScanDataLogCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.File;
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
        ScanDataSettings settings = conversion.getSettings();
        ScanDataLogCreator logCreator = new ScanDataLogCreator(conversion);
        DatabaseLogger<ScanDataLog> logger = new DatabaseLogger<>(logRepository, logCreator);
        ScanDataInterrupter interrupter = new ScanDataInterrupter(conversionRepository, conversion.getId());
        try {
            File scanReportFile = whiteRabbitFacade.generateScanReport(settings, logger, interrupter);
            log.info("Conversion process finished. Scan report file generated!");
            try {
                FileSaveRequest fileSaveRequest = createSaveFileRequest(conversion.getUsername(), scanReportFile);
                FileSaveResponse fileSaveResponse = filesManagerService.saveFile(fileSaveRequest);
                log.info("Scan report file saved via Files Manager service!");
                resultService.saveCompletedResult(fileSaveResponse, conversion.getId());
            } catch (Exception e) {
                log.error("Can not save file via Files-Manager service!");
                throw e;
            } finally {
                scanReportFile.delete();
            }
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            resultService.saveFailedResult(conversion.getId(), e.getMessage());
        } finally {
            settings.destroy();
        }
        return new AsyncResult<>(null);
    }
}

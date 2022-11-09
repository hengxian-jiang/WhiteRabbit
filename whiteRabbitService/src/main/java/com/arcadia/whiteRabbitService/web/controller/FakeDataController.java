package com.arcadia.whiteRabbitService.web.controller;

import com.arcadia.whiteRabbitService.model.fakedata.FakeDataConversion;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataSettings;
import com.arcadia.whiteRabbitService.service.FakeDataConversionService;
import com.arcadia.whiteRabbitService.service.FakeDataService;
import com.arcadia.whiteRabbitService.service.FilesManagerService;
import com.arcadia.whiteRabbitService.service.StorageService;
import com.arcadia.whiteRabbitService.service.error.InternalServerErrorException;
import com.arcadia.whiteRabbitService.service.request.FakeDataRequest;
import com.arcadia.whiteRabbitService.service.request.ScanReportInfo;
import com.arcadia.whiteRabbitService.service.response.ConversionWithLogsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

import static com.arcadia.whiteRabbitService.util.FileUtil.createDirectory;
import static com.arcadia.whiteRabbitService.util.FileUtil.deleteRecursive;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/fake-data")
@RequiredArgsConstructor
@Slf4j
public class FakeDataController {
    private final FakeDataService fakeDataService;
    private final FakeDataConversionService conversionService;
    private final FilesManagerService filesManagerService;
    private final StorageService storageService;

    @PostMapping("/generate")
    public ResponseEntity<FakeDataConversion> generate(@RequestHeader("Username") String username,
                                                       @Validated @RequestBody FakeDataRequest fakeDataRequest) {
        log.info("Rest request to generate Fake Data");

        ScanReportInfo scanReportInfo = fakeDataRequest.getScanReportInfo();
        Resource scanReportResource = filesManagerService.getFile(scanReportInfo.getDataId());

        String project = "fake-data";
        Path scanReportDirectory = Path.of(username, project);
        createDirectory(scanReportDirectory);
        try {
            storageService.store(scanReportResource, scanReportDirectory, scanReportInfo.getFileName());
        } catch (Exception e) {
            log.error("Could not store Scan Report file: {}. Stack trace: {}", e.getMessage(), e.getStackTrace());
            deleteRecursive(scanReportDirectory);
            throw new InternalServerErrorException(e.getMessage(), e);
        }

        FakeDataSettings fakeDataSettings = fakeDataRequest.getSettings();
        fakeDataSettings.setScanReportFileName(scanReportInfo.getFileName());
        fakeDataSettings.setDirectory(scanReportDirectory);
        FakeDataConversion conversion = fakeDataService.createFakeDataConversion(fakeDataSettings, username, project);
        conversionService.runConversion(conversion);

        return ok(conversion);
    }

    @GetMapping("/abort/{conversionId}")
    public ResponseEntity<Void> abort(@RequestHeader("Username") String username,
                                      @PathVariable Long conversionId) {
        log.info("Rest request to abort Fake Data conversion with id {}", conversionId);
        fakeDataService.abort(conversionId, username);
        return noContent().build();
    }

    @GetMapping("/conversion/{conversionId}")
    public ResponseEntity<ConversionWithLogsResponse> conversionInfoAndLogs(@RequestHeader("Username") String username,
                                                                            @PathVariable Long conversionId) {
        log.info("Rest request to get Fake Data Conversion info and logs by Conversion id {}", conversionId);
        return ok(fakeDataService.conversionInfoWithLogs(conversionId, username));
    }
}

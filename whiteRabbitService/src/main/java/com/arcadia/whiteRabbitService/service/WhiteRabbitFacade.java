package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.config.FakeDataDbConfig;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataSettings;
import com.arcadia.whiteRabbitService.model.scandata.ScanDataSettings;
import com.arcadia.whiteRabbitService.model.scandata.ScanDbSettings;
import com.arcadia.whiteRabbitService.service.error.BadRequestException;
import com.arcadia.whiteRabbitService.service.response.TestConnectionResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ohdsi.databases.RichConnection;
import org.ohdsi.whiteRabbit.DbSettings;
import org.ohdsi.whiteRabbit.Interrupter;
import org.ohdsi.whiteRabbit.Logger;
import org.ohdsi.whiteRabbit.TooManyTablesException;
import org.ohdsi.whiteRabbit.scan.SourceDataScan;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;

import static com.arcadia.whiteRabbitService.util.FakeDataGeneratorBuilder.createFakeDataGenerator;
import static com.arcadia.whiteRabbitService.util.FileUtil.*;
import static com.arcadia.whiteRabbitService.util.RichConnectionUtil.createRichConnection;
import static com.arcadia.whiteRabbitService.util.SourceDataScanBuilder.createSourceDataScan;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhiteRabbitFacade {
    private final FakeDataDbConfig fakeDataDbConfig;

    public static final int MAX_TABLES_COUNT = TooManyTablesException.MAX_TABLES_COUNT;

    @PostConstruct
    public void init() {
        createDirectory(scanReportLocation);
    }

    public TestConnectionResultResponse testConnection(ScanDbSettings dbSettings) {
        DbSettings wrSettings = dbSettings.toWhiteRabbitSettings();

        try (RichConnection connection = createRichConnection(wrSettings)) {
            List<String> tableNames = connection.getTableNames(wrSettings.database);
            if (tableNames.isEmpty()) {
                return buildCanNotConnectResponse("Unable to retrieve table names for database " + wrSettings.database);
            } else if (tableNames.size() > MAX_TABLES_COUNT) {
                return buildCanNotConnectResponse("Database contains too many tables. Max count is " + MAX_TABLES_COUNT);
            } else {
                return TestConnectionResultResponse.builder()
                        .canConnect(true)
                        .message(format("Successfully connected to %s database on server %s", wrSettings.database, wrSettings.server))
                        .tableNames(tableNames)
                        .build();
            }
        } catch (Exception e) {
            return buildCanNotConnectResponse("Could not connect to database: " + e.getMessage());
        }
    }

    public Path generateScanReport(ScanDataSettings dbSettings, Logger logger, Interrupter interrupter) throws InterruptedException {
        DbSettings wrSettings = dbSettings.toWhiteRabbitSettings();
        if (wrSettings.tables.size() > MAX_TABLES_COUNT) {
            throw new BadRequestException(format("Database contains too many tables. Max count is %d.", MAX_TABLES_COUNT));
        }
        SourceDataScan sourceDataScan = createSourceDataScan(dbSettings.getScanDataParams(), logger, interrupter);
        Path scanReportFilePath = Path.of(scanReportLocation, generateRandomFileName());
        sourceDataScan.process(wrSettings, scanReportFilePath.toString());
        return scanReportFilePath;
    }

    public void generateFakeData(FakeDataSettings fakeDataSettings, Logger logger, Interrupter interrupter) throws InterruptedException {
        DbSettings wrSettings = createFakeDataWrSettings(fakeDataSettings.getUserSchema());
        try {
            createFakeDataGenerator(logger, interrupter).generateData(
                    wrSettings,
                    fakeDataSettings.getMaxRowCount(),
                    fakeDataSettings.getScanReportPath().toString(),
                    null, // Not needed, it needs if generate fake data to delimited text file
                    fakeDataSettings.getDoUniformSampling(),
                    fakeDataSettings.getUserSchema(),
                    false // False - Tables are created when the report is uploaded to Perseus python service
            );
        } catch (TooManyTablesException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    private DbSettings createFakeDataWrSettings(String schema) {
        return ScanDbSettings.builder()
                .dbType(fakeDataDbConfig.getDbType())
                .server(fakeDataDbConfig.getServer())
                .port(fakeDataDbConfig.getPort())
                .database(fakeDataDbConfig.getDatabase())
                .user(fakeDataDbConfig.getUser())
                .password(fakeDataDbConfig.getPassword())
                .schema(schema)
                .build()
                .toWhiteRabbitSettings();
    }

    private TestConnectionResultResponse buildCanNotConnectResponse(String message) {
        return TestConnectionResultResponse.builder()
                .canConnect(false)
                .message(message)
                .build();
    }
}

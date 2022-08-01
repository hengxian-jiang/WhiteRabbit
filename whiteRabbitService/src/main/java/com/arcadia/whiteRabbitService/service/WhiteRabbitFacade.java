package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.config.FakeDataDbConfig;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataSettings;
import com.arcadia.whiteRabbitService.model.scandata.ScanDataSettings;
import com.arcadia.whiteRabbitService.model.scandata.ScanDbSettings;
import com.arcadia.whiteRabbitService.service.error.ServerErrorException;
import com.arcadia.whiteRabbitService.service.response.TablesInfoResponse;
import com.arcadia.whiteRabbitService.service.response.TestConnectionResultResponse;
import lombok.RequiredArgsConstructor;
import org.ohdsi.databases.RichConnection;
import org.ohdsi.whiteRabbit.DbSettings;
import org.ohdsi.whiteRabbit.Interrupter;
import org.ohdsi.whiteRabbit.Logger;
import org.ohdsi.whiteRabbit.scan.SourceDataScan;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

import static com.arcadia.whiteRabbitService.util.FakeDataGeneratorBuilder.createFakeDataGenerator;
import static com.arcadia.whiteRabbitService.util.FileUtil.*;
import static com.arcadia.whiteRabbitService.util.RichConnectionUtil.createRichConnection;
import static com.arcadia.whiteRabbitService.util.SourceDataScanBuilder.createSourceDataScan;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class WhiteRabbitFacade {
    private final FakeDataDbConfig fakeDataDbConfig;

    @PostConstruct
    public void init() {
        createDirectory(scanReportLocation);
    }

    public TestConnectionResultResponse testConnection(ScanDbSettings dbSettings) {
        DbSettings wrSettings = dbSettings.toWhiteRabbitSettings();

        try (RichConnection connection = createRichConnection(wrSettings)) {
            if (connection.getTableNames(wrSettings.database).isEmpty()) {
                return new TestConnectionResultResponse(false,
                        "Unable to retrieve table names for database " + wrSettings.database);
            }
            return new TestConnectionResultResponse(true,
                    format("Successfully connected to %s database on server %s", wrSettings.database, wrSettings.server));
        } catch (Exception e) {
            return new TestConnectionResultResponse(false,
                    "Could not connect to database: " + e.getMessage());
        }
    }

    public TablesInfoResponse tablesInfo(ScanDbSettings dbSettings) {
        DbSettings wrSettings = dbSettings.toWhiteRabbitSettings();

        try (RichConnection connection = createRichConnection(wrSettings)) {
            return new TablesInfoResponse(connection.getTableNames(wrSettings.database));
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage(), e);
        }
    }

    public Path generateScanReport(ScanDataSettings dbSettings, Logger logger, Interrupter interrupter) throws InterruptedException {
        DbSettings wrSettings = dbSettings.toWhiteRabbitSettings();

        SourceDataScan sourceDataScan = createSourceDataScan(dbSettings.getScanDataParams(), logger, interrupter);
        Path scanReportFilePath = Path.of(scanReportLocation, generateRandomFileName());
        sourceDataScan.process(wrSettings, scanReportFilePath.toString());
        return scanReportFilePath;
    }

    public void generateFakeData(FakeDataSettings fakeDataSettings, Logger logger, Interrupter interrupter) throws InterruptedException {
        DbSettings wrSettings = createFakeDataWrSettings(fakeDataSettings.getUserSchema());

        createFakeDataGenerator(logger, interrupter).generateData(
                wrSettings,
                fakeDataSettings.getMaxRowCount(),
                fakeDataSettings.getScanReportPath().toString(),
                null, // Not needed, it needs if generate fake data to delimited text file
                fakeDataSettings.getDoUniformSampling(),
                fakeDataSettings.getUserSchema(),
                false // False - Tables are created when the report is uploaded to Perseus python service
        );
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
}

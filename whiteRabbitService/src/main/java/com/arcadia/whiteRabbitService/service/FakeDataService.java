package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.model.fakedata.FakeDataConversion;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataSettings;
import com.arcadia.whiteRabbitService.service.response.ConversionWithLogsResponse;

public interface FakeDataService {
    FakeDataConversion findConversionById(Long conversionId, String username);

    FakeDataConversion createFakeDataConversion(FakeDataSettings fakeDataSettings, String username, String project);

    ConversionWithLogsResponse conversionInfoWithLogs(Long conversionId, String username);

    void abort(Long conversionId, String username);
}

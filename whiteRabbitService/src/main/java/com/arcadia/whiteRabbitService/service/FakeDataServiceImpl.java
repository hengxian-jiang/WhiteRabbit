package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.model.fakedata.FakeDataConversion;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataLog;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataSettings;
import com.arcadia.whiteRabbitService.repository.FakeDataConversionRepository;
import com.arcadia.whiteRabbitService.repository.FakeDataLogRepository;
import com.arcadia.whiteRabbitService.service.response.ConversionWithLogsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.arcadia.whiteRabbitService.model.ConversionStatus.ABORTED;
import static com.arcadia.whiteRabbitService.model.ConversionStatus.IN_PROGRESS;
import static com.arcadia.whiteRabbitService.util.ConversionUtil.toResponseWithLogs;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class FakeDataServiceImpl implements FakeDataService {
    private final FakeDataConversionRepository conversionRepository;
    private final FakeDataLogRepository logRepository;

    @Override
    public FakeDataConversion findConversionById(Long conversionId, String username) {
        FakeDataConversion conversion = conversionRepository.findById(conversionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Fake Data Conversion not found by id " + conversionId));
        if (!conversion.getUsername().equals(username)) {
            throw new ResponseStatusException(FORBIDDEN, "Forbidden get other User Fake Data Conversion logs");
        }
        return conversion;
    }

    @Transactional
    @Override
    public FakeDataConversion createFakeDataConversion(FakeDataSettings fakeDataSettings, String username, String project) {
        FakeDataConversion conversion = FakeDataConversion.builder()
                .username(username)
                .project(project)
                .statusCode(IN_PROGRESS.getCode())
                .statusName(IN_PROGRESS.getName())
                .fakeDataSettings(fakeDataSettings)
                .build();
        fakeDataSettings.setFakeDataConversion(conversion);

        return conversionRepository.saveAndFlush(conversion);
    }

    @Override
    public ConversionWithLogsResponse conversionInfoWithLogs(Long conversionId, String username) {
        FakeDataConversion conversion = findConversionById(conversionId, username);
        List<FakeDataLog> logs = logRepository.findAllByFakeDataConversionId(conversionId)
                .stream()
                .sorted(Comparator.comparing(FakeDataLog::getId))
                .collect(Collectors.toList());
        conversion.setLogs(logs);

        return toResponseWithLogs(conversion);
    }

    @Transactional
    @Override
    public void abort(Long conversionId, String username) {
        FakeDataConversion conversion = findConversionById(conversionId, username);
        conversion.setStatus(ABORTED);
        conversionRepository.save(conversion);
    }
}

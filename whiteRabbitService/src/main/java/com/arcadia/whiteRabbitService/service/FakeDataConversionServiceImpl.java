package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.model.fakedata.FakeDataConversion;
import com.arcadia.whiteRabbitService.model.fakedata.FakeDataLog;
import com.arcadia.whiteRabbitService.repository.FakeDataConversionRepository;
import com.arcadia.whiteRabbitService.repository.FakeDataLogRepository;
import com.arcadia.whiteRabbitService.service.interrupt.FakeDataInterrupter;
import com.arcadia.whiteRabbitService.service.log.DatabaseLogger;
import com.arcadia.whiteRabbitService.service.log.FakeDataLogCreator;
import com.arcadia.whiteRabbitService.service.log.LogCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ohdsi.whiteRabbit.Interrupter;
import org.ohdsi.whiteRabbit.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class FakeDataConversionServiceImpl implements FakeDataConversionService {
    private final FakeDataLogRepository logRepository;
    private final FakeDataConversionRepository conversionRepository;
    private final WhiteRabbitFacade whiteRabbitFacade;
    private final FakeDataResultService resultService;

    @Async
    @Override
    public Future<Void> runConversion(FakeDataConversion conversion) {
        LogCreator<FakeDataLog> logCreator = new FakeDataLogCreator(conversion);
        Logger logger = new DatabaseLogger<>(logRepository, logCreator);
        Interrupter interrupter = new FakeDataInterrupter(conversionRepository, conversion.getId());

        try {
            whiteRabbitFacade.generateFakeData(conversion.getFakeDataSettings(), logger, interrupter);
            resultService.saveCompletedResult(conversion.getId());
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("Could not generate Fake Data: " + e.getMessage());
            e.printStackTrace();
            resultService.saveFailedResult(conversion.getId(), e.getMessage());
        } finally {
            conversion.getFakeDataSettings().destroy();
        }
        return new AsyncResult<>(null);
    }
}

package com.arcadia.whiteRabbitService.web.controller;

import com.arcadia.whiteRabbitService.model.fakedata.FakeDataConversion;
import com.arcadia.whiteRabbitService.service.FakeDataConversionService;
import com.arcadia.whiteRabbitService.service.FakeDataService;
import com.arcadia.whiteRabbitService.service.request.FakeDataRequest;
import com.arcadia.whiteRabbitService.service.response.ConversionWithLogsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/fake-data")
@RequiredArgsConstructor
@Slf4j
public class FakeDataController {
    private final FakeDataService fakeDataService;
    private final FakeDataConversionService conversionService;

    @PostMapping("/generate")
    public ResponseEntity<FakeDataConversion> generate(@RequestHeader("Username") String username,
                                                       @Validated @RequestBody FakeDataRequest fakeDataRequest) {
        log.info("Rest request to generate Fake Data");
        FakeDataConversion conversion = fakeDataService.createFakeDataConversion(fakeDataRequest, username);
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

package com.arcadia.whiteRabbitService.web.controller;

import java.io.ByteArrayOutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arcadia.whiteRabbitService.service.ReportGenerationService;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class ReportGeneratorController {
  private final ReportGenerationService reportGenerationService;

  @PostMapping("/word")
    public ResponseEntity<byte[]> generateWordReport_test(@RequestBody JsonNode jsonNode) {
        try {
            ByteArrayOutputStream wordDocument = reportGenerationService.generateWordReport(jsonNode);
            // Return the file as a response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=etl-report.docx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(wordDocument.toByteArray());
        } catch (Exception e) {
            log.debug(String.format("Some bug happens %s", e));
            System.out.println(String.format("Some bug happens %s", e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
  
}

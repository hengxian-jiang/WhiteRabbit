package com.arcadia.whiteRabbitService.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.ohdsi.rabbitInAHat.dataModel.ETL;
import org.ohdsi.rabbitInAHat.dataModel.ETL.FileFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arcadia.whiteRabbitService.service.ReportGenerationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class ReportGeneratorController {
  private final ReportGenerationService reportGenerationService;
  private final ObjectMapper objectMapper;

  @PostMapping("/word")
    public ResponseEntity<byte[]> generateWordReport_test(@RequestBody JsonNode jsonNode) {
        try {
            // Create a temporary file and write the JSON content to it
            Path tempFile = Files.createTempFile("etl-temp", ".json");
            File file = tempFile.toFile();
            
            // Write the JSON data to the temporary file
            objectMapper.writeValue(file, jsonNode);

            ETL etl = ETL.fromFile(file.getAbsolutePath(), FileFormat.Json);

            ByteArrayOutputStream wordDocument = reportGenerationService.generateWordReport(etl);

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

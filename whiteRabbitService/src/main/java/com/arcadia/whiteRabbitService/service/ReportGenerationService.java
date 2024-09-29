package com.arcadia.whiteRabbitService.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.ohdsi.rabbitInAHat.ETLWordDocumentGenerator;
import org.ohdsi.rabbitInAHat.ObjectExchange;
import org.ohdsi.rabbitInAHat.dataModel.ETL;
import org.ohdsi.rabbitInAHat.dataModel.ETL.FileFormat;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {
  private final ObjectMapper objectMapper;
  public ByteArrayOutputStream generateWordReport(JsonNode jsonNode) throws IOException{
    try {
       // Create a temporary file and write the JSON content to it
        Path tempFile = Files.createTempFile("etl-temp", ".json");
        File file = tempFile.toFile();
        
        // Write the JSON data to the temporary file
        objectMapper.writeValue(file, jsonNode);

        ETL etl = ETL.fromFile(file.getAbsolutePath(), FileFormat.Json);
        ObjectExchange.etl = etl;
        return ETLWordDocumentGenerator.generate(ObjectExchange.etl, "report");
    } catch (Exception e) {
        log.debug(String.format("Error occurs %s", e));
        System.out.println(String.format("Error occurs %s", e));
        return null;
    }
  }
}

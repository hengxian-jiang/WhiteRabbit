package com.arcadia.whiteRabbitService.service;

import java.io.ByteArrayOutputStream;

import org.ohdsi.rabbitInAHat.ETLWordDocumentGenerator;
import org.ohdsi.rabbitInAHat.ObjectExchange;
import org.ohdsi.rabbitInAHat.dataModel.ETL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {
  public ByteArrayOutputStream generateWordReport(ETL etl) {
    ObjectExchange.etl = etl;
    return ETLWordDocumentGenerator.generate(ObjectExchange.etl, "report");
  }
}

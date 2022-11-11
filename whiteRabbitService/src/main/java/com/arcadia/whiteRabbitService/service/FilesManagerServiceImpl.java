package com.arcadia.whiteRabbitService.service;

import com.arcadia.whiteRabbitService.service.error.InternalServerErrorException;
import com.arcadia.whiteRabbitService.service.request.FileSaveRequest;
import com.arcadia.whiteRabbitService.service.response.FileSaveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilesManagerServiceImpl implements FilesManagerService {
    @Value("${files-manager-url}")
    private String filesManagerUrl;

    private final RestTemplate restTemplate;

    @Override
    public Resource getFile(Long userDataId) {
        try {
            return restTemplate.getForObject(
                    filesManagerUrl + "/api/{userDataId}",
                    ByteArrayResource.class,
                    userDataId
            );
        } catch (RestClientException e) {
            log.error("Error when connect to File Manager: {}. Stack trace: {}", e.getMessage(), e.getStackTrace());
            throw new InternalServerErrorException("Error when connect to File Manager: " + e.getMessage(), e);
        }
    }

    @Override
    public FileSaveResponse saveFile(FileSaveRequest model) {
        log.info("Sending Rest request to save scan report file...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("username", model.getUsername());
        map.add("dataKey", model.getDataKey());
        map.add("file", model.getFile());

        try {
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
            ResponseEntity<FileSaveResponse> responseEntity = restTemplate.postForEntity(
                    filesManagerUrl + "/api",
                    request,
                    FileSaveResponse.class
            );
            return responseEntity.getBody();
        } catch (RestClientException e) {
            log.error("Error when connect to File Manager: {}. Stack trace: {}", e.getMessage(), e.getStackTrace());
            throw new InternalServerErrorException("Error when connect to File Manager: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            restTemplate.delete(filesManagerUrl + "/api/${key}", key);
        } catch (RestClientException e) {
            log.error("Error when connect to File Manager: {}. Stack trace: {}", e.getMessage(), e.getStackTrace());
            throw new InternalServerErrorException("Error when connect to File Manager: " + e.getMessage(), e);
        }
    }
}

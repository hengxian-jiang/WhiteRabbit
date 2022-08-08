package com.arcadia.whiteRabbitService.service.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionResultResponse {
    private boolean canConnect;

    private String message;

    private List<String> tableNames;
}

package com.arcadia.whiteRabbitService.service.log;

import com.arcadia.whiteRabbitService.model.LogStatus;

public interface LogCreator<T> {
    T create(String message, LogStatus status, Integer percent);
}

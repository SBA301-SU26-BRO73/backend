package com.sba301.backend.config.exception;

import com.sba301.backend.common.enums.ErrorEnum;

import lombok.Getter;


@Getter
public class AppException extends RuntimeException {
    private final ErrorEnum errorEnum;
    private final String customMessage;

    public AppException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.errorEnum = errorEnum;
        this.customMessage = errorEnum.getMessage();
    }

    public AppException(ErrorEnum errorEnum, String customMessage) {
        super(customMessage);
        this.errorEnum = errorEnum;
        this.customMessage = customMessage;
    }
}


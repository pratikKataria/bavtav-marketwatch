package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApiErrorResponse {

    private String message;
    private String errorCode;
    private List<String> details;
    private LocalDateTime timestamp;
}
package com.submate.submate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, Integer remainingAttempts) {

    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }
}

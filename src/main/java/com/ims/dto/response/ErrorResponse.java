package com.ims.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final List<FieldError> fieldErrors;
    private final Map<String, Object> details;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldError {
        private final String field;
        private final Object rejectedValue;
        private final String message;
    }
}

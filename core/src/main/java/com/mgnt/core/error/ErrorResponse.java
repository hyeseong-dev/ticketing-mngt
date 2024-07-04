package com.mgnt.core.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;
    private List<FieldError> errors;
    private String code;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        public static List<FieldError> of(String field, String value, String reason) {
            return List.of(new FieldError(field, value, reason));
        }

        public static List<FieldError> of(BindingResult bindingResult) {
            List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() != null ? error.getRejectedValue().toString() : "",
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getMessage(),
                errorCode.getStatus(),
                List.of(),
                errorCode.getCode()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(
                errorCode.getMessage(),
                errorCode.getStatus(),
                errors,
                errorCode.getCode()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(
                errorCode.getMessage(),
                errorCode.getStatus(),
                FieldError.of(bindingResult),
                errorCode.getCode()
        );
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"message\": \"Internal server error\", \"status\": 500, \"errors\": [], \"code\": \"INTERNAL_SERVER_ERROR\"}";
        }
    }
}

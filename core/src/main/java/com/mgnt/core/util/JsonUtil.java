package com.mgnt.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import org.apache.logging.log4j.Level;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> String convertToJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR, "Error converting object to JSON", Level.ERROR);
        }
    }

    public static <T> T convertFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR, "Error converting JSON to object", Level.ERROR);
        }
    }
}
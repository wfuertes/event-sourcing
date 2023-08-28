package com.wfuertes.infra.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

    private final ObjectMapper mapper;

    public JsonParser() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    }

    public <T> String toJson(T value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException err) {
            throw new RuntimeException(err);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException err) {
            throw new RuntimeException(err);
        }
    }
}

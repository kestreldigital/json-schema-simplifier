package com.kestreldigital.jsonschema.simplifier.plugin.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonUtils {

    private final ObjectMapper objectMapper;

    public JsonUtils() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public String toJson(Object pojo) throws JsonProcessingException {
        return objectMapper.writeValueAsString(pojo);
    }

    public void toFile(String filePath, Object pojo) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), pojo);
    }

    public <T> T fromJson(String json, Class<T> targetClass) throws IOException {
        return objectMapper.readValue(json, targetClass);
    }

    public <T> T fromFile(File jsonFile, Class<T> targetClass) throws IOException {
        return objectMapper.readValue(jsonFile, targetClass);
    }

}
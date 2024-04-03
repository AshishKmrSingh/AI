package com.ashish.hackathon.ignitedroids;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ashish.hackathon.ignitedroids.model.ProjectConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private static final ObjectMapper mapper = configureMapper();
    private static ProjectConfig projectConfig;
    public static ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    private static ObjectMapper configureMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static ObjectMapper getMapper() {
        return mapper;
    }

    public static ProjectConfig readConfigFile(File file) throws IOException {
        logger.info("Reading config file: " + file.getName());
        projectConfig = mapper.readValue(file, ProjectConfig.class);
        return projectConfig;
    }
}

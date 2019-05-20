package com.kestreldigital.jsonschema.simplifier.plugin.draft07;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kestreldigital.jsonschema.simplifier.plugin.SchemaSimplifier;
import org.apache.maven.plugin.logging.Log;
import org.jsonschema.model.specification.draft07.SchemaObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Draft07SchemaSimplifier implements SchemaSimplifier {

    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Map<String, SchemaObject>> definitions = new HashMap<>();

    private Log log;

    public Draft07SchemaSimplifier(Log log) {
        this.log = log;
    }

    @Override
    public void process(List<File> schemaFiles, String outputPath) throws IOException {
        for (File schemaFile : schemaFiles) {
            final SchemaObject schema = objectMapper.readValue(schemaFile, SchemaObject.class);
            final String fileName = schemaFile.getName();
            log.info("Read file " + fileName + " to json schema.");
            if (schema.getDefinitions() != null && schema.getDefinitions().getAdditionalProperties() != null) {
                System.out.println("File contains definitions.");
                definitions.put(fileName, new HashMap<>(schema.getDefinitions().getAdditionalProperties()));
            }
        }
        for (String fileName : definitions.keySet()) {
            for (String definitionName : definitions.get(fileName).keySet()) {
                System.out.println("File name: " + fileName + ", definition name: " + definitionName);
            }
        }
        // 1. Go through definitions, resolving references.
        // 2. Go through schemaFiles, resolving references and removing unknown properties.
        // 3. Convert the cleaned-up SchemaObject back to json & write to the output folder.
    }
}

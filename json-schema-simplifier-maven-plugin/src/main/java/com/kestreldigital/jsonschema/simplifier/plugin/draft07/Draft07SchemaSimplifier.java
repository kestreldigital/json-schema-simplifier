package com.kestreldigital.jsonschema.simplifier.plugin.draft07;

import com.kestreldigital.jsonschema.simplifier.plugin.SchemaSimplifier;
import com.kestreldigital.jsonschema.simplifier.plugin.utils.JsonUtils;
import com.kestreldigital.jsonschema.simplifier.plugin.utils.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.jsonschema.model.specification.draft07.SchemaObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Draft07SchemaSimplifier implements SchemaSimplifier {

    private final Map<String, Map<String, SchemaObject>> definitions = new HashMap<>();

    private final Map<String, SchemaObject> schemae = new HashMap<>();

    private final JsonUtils jsonUtils;

    private final Log log;

    public Draft07SchemaSimplifier(Log log) {
        this.log = log;
        jsonUtils = new JsonUtils();
    }

    @Override
    public void process(List<File> schemaFiles, String outputPath) throws IOException {
        for (File schemaFile : schemaFiles) {
            final SchemaObject schema = jsonUtils.fromFile(schemaFile, SchemaObject.class);
            final String fileName = schemaFile.getName();
            log.debug("Read file " + fileName + " to json schema.");
            schemae.put(fileName, schema);
            if (schema.getDefinitions() != null && schema.getDefinitions().getAdditionalProperties() != null) {
                log.debug("File contains definitions.");
                definitions.put(fileName, new HashMap<>(schema.getDefinitions().getAdditionalProperties()));
            }
        }
        // 1. Go through definitions, resolving references.
        for (String fileName : definitions.keySet()) {
            final Map<String, SchemaObject> thisFileDefinitions = definitions.get(fileName);
            for (String definitionName : thisFileDefinitions.keySet()) {
                log.info("Found definition: " + fileName + "#/definitions/" + definitionName);
                final SchemaObject resolvedDefinition = resolveReferences(fileName, thisFileDefinitions.get(definitionName));
                thisFileDefinitions.put(definitionName, resolvedDefinition);
                log.debug("Resolved definition: " + jsonUtils.toJson(resolvedDefinition));
            }
        }
        // 2. Go through schemaFiles, resolving references and removing unknown properties.
        for (Map.Entry<String, SchemaObject> schema : schemae.entrySet()) {
            final String fileName = schema.getKey();
            final SchemaObject cleanedUpSchema = resolveReferences(fileName, schema.getValue());
            // 3. Convert the cleaned-up SchemaObject back to json & write to the output folder.
            jsonUtils.toFile(outputPath + "/" + fileName, cleanedUpSchema);
            log.info("Processed file " + fileName);
        }
    }

    private SchemaObject resolveReferences(String fileName, SchemaObject schemaObject) throws IOException {
        if (isReference(schemaObject)) {
            log.debug("Found reference: " + schemaObject.get$ref());
            final SchemaObject referenceTarget = findReference(fileName, schemaObject.get$ref());
            schemaObject.setType(referenceTarget.getType());
            schemaObject.setMaxLength(referenceTarget.getMaxLength());
            schemaObject.setPattern(referenceTarget.getPattern());
            schemaObject.setMinimum(referenceTarget.getMinimum());
            schemaObject.setMinItems(referenceTarget.getMinItems());
            schemaObject.setMinLength(referenceTarget.getMinLength());
            schemaObject.setEnum(referenceTarget.getEnum());
            schemaObject.setFormat(referenceTarget.getFormat());
            schemaObject.setMaxItems(referenceTarget.getMaxItems());
            schemaObject.setProperties(referenceTarget.getProperties());
            if (StringUtils.isEmpty(schemaObject.getDescription())) {
                schemaObject.setDescription(referenceTarget.getDescription());
            }
            schemaObject.set$ref(null);
            schemaObject.setItems(referenceTarget.getItems());
            return schemaObject;
        }
        if (hasProperties(schemaObject)) {
            final Map<String, SchemaObject> properties = schemaObject.getProperties().getAdditionalProperties();
            for (Map.Entry<String, SchemaObject> property : properties.entrySet()) {
                properties.put(property.getKey(), resolveReferences(fileName, property.getValue()));
            }
        }
        if (schemaObject.getItems() != null) {
            schemaObject.setItems(resolveReferences(fileName, schemaObject.getItems()));
        }
        schemaObject.getAdditionalProperties().clear();
        return schemaObject;
    }

    private SchemaObject findReference(String currentFileName, String reference) throws IOException {
        if (reference.contains("#")) {
            String referenceFileName = currentFileName;
            String referencePath = reference.replace("#/definitions/", "");
            if (!reference.startsWith("#")) {
                referenceFileName = reference.substring(0, reference.indexOf("#"));
                referencePath = reference.replace(referenceFileName + "#/definitions/", "");
            }
            if (definitions.containsKey(referenceFileName)) {
                if (definitions.get(referenceFileName).containsKey(referencePath)) {
                    return definitions.get(referenceFileName).get(referencePath);
                }
            }
            log.error("Unable to resolve reference " + reference + " in file " + currentFileName);
            log.info("Interpreted file name: " + referenceFileName + " and path " + referencePath);
        } else {
            if (schemae.containsKey(reference)) {
                return schemae.get(reference);
            }
            log.error("Reference " + reference + " does not contain a # symbol and is not a filename - not sure how to process.");
        }

        throw new IOException("JSON schema file contained unresolvable reference" + reference + ".");
    }

    private boolean hasProperties(SchemaObject schemaObject) {
        return schemaObject.getProperties() != null && schemaObject.getProperties().getAdditionalProperties() != null;
    }

    private boolean isReference(SchemaObject schemaObject) {
        return !StringUtils.isEmpty(schemaObject.get$ref());
    }

}

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

    private final Map<String, Map<String, SchemaObject>> definitions;

    private final JsonUtils jsonUtils;

    private final Log log;

    public Draft07SchemaSimplifier(Log log) {
        this.log = log;
        definitions = new HashMap<>();
        jsonUtils = new JsonUtils();
    }

    @Override
    public void process(List<File> schemaFiles, String outputPath) throws IOException {
        for (File schemaFile : schemaFiles) {
            final SchemaObject schema = jsonUtils.fromFile(schemaFile, SchemaObject.class);
            final String fileName = schemaFile.getName();
            log.info("Read file " + fileName + " to json schema.");
            if (schema.getDefinitions() != null && schema.getDefinitions().getAdditionalProperties() != null) {
                System.out.println("File contains definitions.");
                definitions.put(fileName, new HashMap<>(schema.getDefinitions().getAdditionalProperties()));
            }
        }
        // 1. Go through definitions, resolving references.
        for (String fileName : definitions.keySet()) {
            final Map<String, SchemaObject> thisFileDefinitions = definitions.get(fileName);
            for (String definitionName : thisFileDefinitions.keySet()) {
                log.info("File name: " + fileName + ", definition name: " + definitionName);
                final SchemaObject resolvedDefinition = resolveReferences(fileName, thisFileDefinitions.get(definitionName));
                thisFileDefinitions.put(definitionName, resolvedDefinition);
                log.info("Resolved definition: " + jsonUtils.toJson(resolvedDefinition));
            }
        }
        // 2. Go through schemaFiles, resolving references and removing unknown properties.
        for (File schemaFile : schemaFiles) {
            final SchemaObject schemaObject = jsonUtils.fromFile(schemaFile, SchemaObject.class);
            final SchemaObject cleanedUpSchema = cleanUpSchema(schemaFile.getName(), schemaObject);
            // 3. Convert the cleaned-up SchemaObject back to json & write to the output folder.
            jsonUtils.toFile(outputPath + "/" + schemaFile.getName(), cleanedUpSchema);
        }
    }

    private SchemaObject cleanUpSchema(String fileName, SchemaObject originalSchema) throws IOException {
        final SchemaObject cleanSchema = resolveReferences(fileName, originalSchema);
        cleanSchema.getAdditionalProperties().clear();

        return cleanSchema;
    }

    private SchemaObject resolveReferences(String fileName, SchemaObject schemaObject) throws IOException {
        if (isReference(schemaObject)) {
            log.info("Found reference: " + schemaObject.get$ref());
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
            schemaObject.set$ref(null);
            return schemaObject;
        }
        if (hasProperties(schemaObject)) {
            final Map<String, SchemaObject> properties = schemaObject.getProperties().getAdditionalProperties();
            for (Map.Entry<String, SchemaObject> property : properties.entrySet()) {
                properties.put(property.getKey(), resolveReferences(fileName, property.getValue()));
            }
        }
        // TODO: What about array items?
        return schemaObject;
    }

    private SchemaObject findReference(String currentFileName, String reference) throws IOException {
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
        throw new IOException("JSON schema file contained unresolvable reference.");
    }

    private boolean hasProperties(SchemaObject schemaObject) {
        return schemaObject.getProperties() != null && schemaObject.getProperties().getAdditionalProperties() != null;
    }

    private boolean isReference(SchemaObject schemaObject) {
        return !StringUtils.isEmpty(schemaObject.get$ref());
    }

    private SchemaObject buildBlankSchema() {
        final SchemaObject schemaObject = new SchemaObject();
        schemaObject.setAllOf(null);
        schemaObject.setAnyOf(null);
        schemaObject.setEnum(null);
        schemaObject.setExamples(null);
        schemaObject.setOneOf(null);
        schemaObject.setReadOnly(null);
        schemaObject.setRequired(null);
        schemaObject.setUniqueItems(null);
        return schemaObject;
    }

}

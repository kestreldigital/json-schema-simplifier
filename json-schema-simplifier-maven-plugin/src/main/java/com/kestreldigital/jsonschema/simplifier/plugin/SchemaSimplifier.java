package com.kestreldigital.jsonschema.simplifier.plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SchemaSimplifier {

    void process(List<File> schemaFiles, String outputPath) throws IOException;

}

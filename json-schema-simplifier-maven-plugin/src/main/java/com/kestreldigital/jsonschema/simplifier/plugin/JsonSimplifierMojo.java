package com.kestreldigital.jsonschema.simplifier.plugin;

import com.kestreldigital.jsonschema.simplifier.plugin.draft07.Draft07SchemaSimplifier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name="simplify")
public class JsonSimplifierMojo extends AbstractMojo {

    @Parameter(property="sourceDirectory")
    private String sourceDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceDirectory == null || "".equals(sourceDirectory)) {
            throw new MojoExecutionException("No source path specified - configuration parameter sourceDirectory is required.");
        }
        sourceDirectory = FilenameUtils.normalize(sourceDirectory);
        getLog().info("Searching for JSON schema files on path " + sourceDirectory);
        final File dir = new File(sourceDirectory);
        final File[] directoryListing = dir.listFiles();
        final List<File> schemaFiles = new ArrayList<>();
        if (directoryListing == null) {
            throw new MojoExecutionException("Invalid source path - path " + sourceDirectory + " was not found.");
        }
        for (File child : directoryListing) {
            if (child.getPath().endsWith(".json")) {
                getLog().debug("Found json schema file: " + child.getName());
                schemaFiles.add(child);
            }
        }
        SchemaSimplifier schemaSimplifier = new Draft07SchemaSimplifier(getLog());
        try {
            schemaSimplifier.process(schemaFiles, "");
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to parse JSON files.", e);
        }
    }
}

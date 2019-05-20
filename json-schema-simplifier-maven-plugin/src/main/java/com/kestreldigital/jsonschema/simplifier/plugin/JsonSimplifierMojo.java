package com.kestreldigital.jsonschema.simplifier.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mojo(name="simplify")
public class JsonSimplifierMojo extends AbstractMojo {

    @Parameter(property="sourceDirectory")
    private String sourceDirectory;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (sourceDirectory == null || "".equals(sourceDirectory)) {
            throw new MojoExecutionException("No source path specified - configuration parameter sourceDirectory is required.");
        }

        sourceDirectory = FilenameUtils.normalize(sourceDirectory);

        getLog().info("Searching for JSON schema files on path " + sourceDirectory);

        File dir = new File(sourceDirectory);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getPath().endsWith(".json")) {
                    getLog().info("Found json schema file: " + child.getName());
                    try {
                        final String schemaBody = new String(Files.readAllBytes(child.toPath()));

                    } catch (IOException e) {
                        throw new MojoExecutionException("Unable to read schema file " + child.getName());
                    }
                }
            }
        } else {
            throw new MojoExecutionException("Invalid source path - path " + sourceDirectory + " was not found.");
        }

    }
}

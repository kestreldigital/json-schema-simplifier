package com.kestreldigital.jsonschema.simplifier.plugin;

import com.kestreldigital.jsonschema.simplifier.plugin.draft07.Draft07SchemaSimplifier;
import com.kestreldigital.jsonschema.simplifier.plugin.utils.StringUtils;
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

    @Parameter(property="targetDirectory")
    private String targetDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (StringUtils.isEmpty(sourceDirectory)) {
            throw new MojoExecutionException("No source path specified - configuration parameter sourceDirectory is required.");
        }
        sourceDirectory = FilenameUtils.normalize(sourceDirectory);
        if (StringUtils.isEmpty(targetDirectory)) {
            throw new MojoExecutionException("No target path specified - configuration parameter sourceDirectory is required.");
        }
        targetDirectory = FilenameUtils.normalize(targetDirectory);
        getLog().info("Searching for JSON schema files on path " + sourceDirectory);
        final File dir = new File(sourceDirectory);
        final File[] directoryListing = dir.listFiles();
        final List<File> schemaFiles = new ArrayList<>();
        if (directoryListing == null) {
            throw new MojoExecutionException("Invalid source path - path " + sourceDirectory + " was not found.");
        }
        for (File child : directoryListing) {
            if (isJsonFile(child)) {
                getLog().debug("Found json schema file: " + child.getName());
                schemaFiles.add(child);
            }
        }
        SchemaSimplifier schemaSimplifier = new Draft07SchemaSimplifier(getLog());
        try {
            schemaSimplifier.process(schemaFiles, targetDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to parse JSON files.", e);
        }
    }

    private boolean isJsonFile(File child) {
        return child.getPath().endsWith(".json");
    }
}

# JSON schema simplifier Maven plugin

A simple Maven plugin to generate simplified JSON schema from any standard JSON schema (tested up to draft 7).

The simplification process:

* Removes non-standard properties (e.g. javaType);
* Resolves references to other files/definitions, so that each output JSON schema file is standalone.

This is useful where you need to reuse your JSON schemae in applications which require simpler implementations, e.g. configuring models in AWS API Gateway or referencing from n OpenAPI3 interface specification.

## Usage:

Currently, this tool is only set up to be used as a Maven plugin. Add this to the build/plugins section of your pom file:

    <plugin>
        <groupId>com.kestreldigital</groupId>
        <artifactId>json-schema-simplifier-maven-plugin</artifactId>
        <version>0.0.1</version>
        <configuration>
            <sourceDirectory>${basedir}/json-schema</sourceDirectory>
        </configuration>
        <executions>
            <execution>
                <phase>compile</phase>
                <goals>
                    <goal>simplify</goal>
                </goals>
            </execution>
        </executions>
    </plugin>



## Configuration:

* *sourceDirectory*: Path of the folder containing your JSON schema files (required).
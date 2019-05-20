# JSON schema simplifier Maven plugin

A simple Maven plugin to generate simplified JSON schema from any standard JSON schema (tested up to draft 7).

The simplification process:

* Removes non-standard properties (e.g. javaType);
* Resolves references to other files/definitions, so that each output JSON schema file is standalone.
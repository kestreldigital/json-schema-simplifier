    package com.kestreldigital.jsonschema.simplifier.plugin;
     
    import org.apache.maven.plugin.AbstractMojo;
    import org.apache.maven.plugin.MojoExecutionException;
    import org.apache.maven.plugins.annotations.Mojo;
    import org.apache.maven.plugins.annotations.Parameter;
     
    /**
     * Says "Hi" to the user.
     *
     */
    @Mojo( name = "sayhi")
    public class GreetingMojo extends AbstractMojo {
        
        @Parameter(property="greeting")
        private String greeting;

        public void execute() throws MojoExecutionException {
            getLog().info("Hello, world.");
            getLog().info("Your custom greeting: " + greeting);
        }

    }
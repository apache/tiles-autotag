/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tiles.autotag.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.tiles.autotag.core.OutputLocator;
import org.apache.tiles.autotag.generate.TemplateGenerator;
import org.apache.tiles.autotag.generate.TemplateGeneratorFactory;
import org.apache.tiles.autotag.model.TemplateSuite;
import org.apache.velocity.app.VelocityEngine;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

/**
 * Abstract class to generate boilerplate code starting from template model classes.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractGenerateMojo extends AbstractMojo {
	/**
     * The position of the template suite XML descriptor.
     */
    static final String META_INF_TEMPLATE_SUITE_XML = "META-INF/template-suite.xml";

    /**
     * The classpath elements.
     */
    @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
    List<String> classpathElements;

    /**
     * Location of the generated classes.
     */
	@Parameter(defaultValue = "${project.build.directory}/autotag-classes", required = true)
	File classesOutputDirectory;

    /**
     * Location of the generated resources.
     */
	@Parameter(defaultValue = "${project.build.directory}/autotag-resources", required = true)
    File resourcesOutputDirectory;

    /**
     * Name of the request class.
     */
	@Parameter(defaultValue = "org.apache.tiles.request.Request", required = true)
    String requestClass;

    /**
     * Name of the package.
     */
	@Parameter(required = true)
    String packageName;

	@Component
    MavenProject project;

	@Component
    BuildContext buildContext;

	OutputLocator classesOutputLocator;
	OutputLocator resourcesOutputLocator;
	
    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        try {
        	TemplateSuite suite;
        	URLConnection templateSuite = findTemplateSuiteDescriptor();
        	long lastModified = templateSuite.getLastModified();
        	InputStream stream = templateSuite.getInputStream();
            try {
	            XStream xstream = new XStream(new Sun14ReflectionProvider());
	            suite = (TemplateSuite) xstream.fromXML(stream);
            } finally {
	            stream.close();
            }
            classesOutputLocator = new MavenOutputLocator(classesOutputDirectory, lastModified);
            resourcesOutputLocator = new MavenOutputLocator(resourcesOutputDirectory, lastModified);
            Properties props = new Properties();
            InputStream propsStream = getClass().getResourceAsStream("/org/apache/tiles/autotag/velocity.properties");
            props.load(propsStream);
            propsStream.close();
            TemplateGenerator generator = createTemplateGeneratorFactory(
                    new VelocityEngine(props)).createTemplateGenerator();
            generator.generate(packageName, suite, getParameters(), getRuntimeClass(), requestClass);
            if (generator.isGeneratingResources()) {
            	buildContext.refresh(resourcesOutputDirectory);
                addResourceDirectory(resourcesOutputDirectory.getAbsolutePath());
            }
            if (generator.isGeneratingClasses()) {
            	buildContext.refresh(classesOutputDirectory);
                addCompileSourceRoot(classesOutputDirectory.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("error", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("error", e);
        }
    }

	private void addResourceDirectory(String directory) {
		boolean addResource = true;
		@SuppressWarnings("unchecked")
		List<Resource> resources = project.getResources();
		for(Resource resource: resources) {
			if(directory.equals(resource.getDirectory())) {
				addResource = false;
			}
		}
		if(addResource) {
		    Resource resource = new Resource();
		    resource.setDirectory(directory);
		    project.addResource(resource);
		}
	}

	private void addCompileSourceRoot(String directory) {
		boolean addResource = true;
		@SuppressWarnings("unchecked")
		List<String> roots = project.getCompileSourceRoots();
		for(String root: roots) {
			if(directory.equals(root)) {
				addResource = false;
			}
		}
		if(addResource) {
		    project.addCompileSourceRoot(directory);
		}
	}


	/**
     * Creates a template generator factory.
     *
     * @param velocityEngine The Velocity engine.
     * @return The template generator factory.
     */
    protected abstract TemplateGeneratorFactory createTemplateGeneratorFactory(VelocityEngine velocityEngine);

    /**
     * Returns the map of parameters.
     *
     * @return The parameters.
     */
    protected abstract Map<String, String> getParameters();

    /**
     * Searches for the template suite descriptor in all dependencies and sources.
     *
     * @return The inputstream of the identified descriptor.
     * @throws IOException If something goes wrong.
     */
    private URLConnection findTemplateSuiteDescriptor() throws IOException {
        URL[] urls = new URL[classpathElements.size()];
        int i = 0;
        for ( String classpathElement: classpathElements )
        {
            urls[i++] = new File(classpathElement).toURI().toURL();
        }

        ClassLoader cl = new URLClassLoader( urls );
        return cl.getResource(META_INF_TEMPLATE_SUITE_XML).openConnection();
    }

    /**
     * Name of the Runtime class.
     * @return The name of the Runtime class.
     */
    protected abstract String getRuntimeClass();

    private final class MavenOutputLocator implements OutputLocator {
    	
    	private File outputDirectory;
    	private long sourceLastModified;
    	
    	private MavenOutputLocator(File outputDirectory, long sourceLastModified) {
    		this.outputDirectory = outputDirectory;
    		this.sourceLastModified = sourceLastModified;
    	}
    	
		@Override
		public OutputStream getOutputStream(String resourcePath)
				throws IOException {
			File target = new File(outputDirectory, resourcePath);
			target.getParentFile().mkdirs();
			return buildContext.newFileOutputStream(target);
		}

		@Override
		public boolean isUptodate(String resourcePath) {
			File target = new File(outputDirectory, resourcePath);
			return target.exists() && target.lastModified() > sourceLastModified;
		}
	}
}

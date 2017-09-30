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

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.tiles.autotag.core.QDoxTemplateSuiteFactory;
import org.apache.tiles.autotag.model.TemplateSuite;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.thoughtworks.xstream.XStream;

/**
 * Creates a descriptor for the template model in XML format.
 */
@Mojo(name = "create-descriptor", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class CreateDescriptorMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
	@Parameter(defaultValue = "${project.build.directory}/autotag-template-suite", required = true)
    File outputDirectory;

    /**
     * Location of the file.
     */
	@Parameter(property = "project.build.sourceDirectory", required = true)
    File sourceDirectory;

    /**
     * included files.
     */
	@Parameter
    Set<String> includes;

    /**
     * The name of the template.
     */
	@Parameter(required = true)
    String name;

    /**
     * The documentation of the suite.
     */
	@Parameter
    String documentation;

    /**
     * Excluded files.
     */
	@Parameter
    Set<String> excludes;

    /**
     * Name of the request class.
     */
	@Parameter(defaultValue="org.apache.tiles.request.Request", required = true)
    String requestClass;

	@Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

	@Component
    BuildContext buildContext;

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        try {
            String[] fileNames = getSourceInclusionScanner().getIncludedFiles();
            File dir = new File(outputDirectory, "META-INF");
            if(!dir.exists()) {
            	dir.mkdirs();
            	buildContext.refresh(dir);
            }
            File outputFile = new File(dir, "template-suite.xml");
            boolean uptodate = outputFile.exists();
            File[] files = new File[fileNames.length];
            for(int i=0; i<fileNames.length; i++) {
            	files[i] = new File(sourceDirectory, fileNames[i]);
            	uptodate &= buildContext.isUptodate(outputFile, files[i]);
            }
            if(!uptodate) {
                createDescriptor(outputFile, files);
			}
            addResourceDirectory(outputDirectory.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("error", e);
        }
    }

	private void createDescriptor(File outputFile, File[] files)
			throws IOException {
		QDoxTemplateSuiteFactory factory = new QDoxTemplateSuiteFactory(files);
		factory.setSuiteName(name);
		factory.setSuiteDocumentation(documentation);
		factory.setRequestClass(requestClass);
		TemplateSuite suite = factory.createTemplateSuite();
		XStream xstream = new XStream();
		OutputStream os = buildContext.newFileOutputStream(outputFile);
		Writer writer = new OutputStreamWriter(os);
		xstream.toXML(suite, writer);
		writer.close();
		os.close();
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

    /**
     * Creates a source inclusion scanner.
     *
     * @return The inclusion scanner.
     */
    private Scanner getSourceInclusionScanner() {
    	Scanner scanner = buildContext.newScanner( sourceDirectory );
        if (includes == null) {
            includes = new HashSet<String>();
        }
        if (excludes == null) {
            excludes = new HashSet<String>();
        }

        if (includes.isEmpty()) {
            scanner.setIncludes(new String[] {"**/*Model.java"});
        }
        else {
        	scanner.setIncludes(includes.toArray(new String[includes.size()]));
        }
        if (!excludes.isEmpty()) {
        	scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        }
        scanner.scan();
        return scanner;
    }
}

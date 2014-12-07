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
package org.apache.tiles.autotag.generate;

import java.util.ArrayList;
import java.util.List;

import org.apache.tiles.autotag.core.OutputLocator;
import org.apache.tiles.autotag.generate.BasicTemplateGenerator.TCGeneratorDirectoryPair;
import org.apache.tiles.autotag.generate.BasicTemplateGenerator.TSGeneratorDirectoryPair;

/**
 * Builds a {@link TemplateGenerator}.
 *
 * @version $Rev$ $Date$
 */
public class TemplateGeneratorBuilder {

    /**
     * The template suite generators.
     */
    private List<TSGeneratorDirectoryPair> templateSuiteGenerators;

    /**
     * The template class generators.
     */
    private List<TCGeneratorDirectoryPair> templateClassGenerators;

    /**
     * Indicates that this generator generates resources.
     */
    private boolean generatingResources = false;

    /**
     * Indicates that this generator generates classes.
     */
    private boolean generatingClasses = false;

    /**
     * The classes output directory.
     */
    private OutputLocator classesOutputLocator;

    /**
     * The resources output directory.
     */
    private OutputLocator resourcesOutputLocator;

    /**
     * Constructor.
     */
    private TemplateGeneratorBuilder() {
        templateSuiteGenerators = new ArrayList<BasicTemplateGenerator.TSGeneratorDirectoryPair>();
        templateClassGenerators = new ArrayList<BasicTemplateGenerator.TCGeneratorDirectoryPair>();
    }

    /**
     * Creates a new instance of the builder.
     *
     * @return A new instance of the builder.
     */
    public static TemplateGeneratorBuilder createNewInstance() {
        return new TemplateGeneratorBuilder();
    }

    /**
     * Sets the classes output directory.
     *
     * @param classesOutputDirectory The classes output directory.
     * @return This instance.
     */
    public TemplateGeneratorBuilder setClassesOutputLocator(OutputLocator classesOutputLocator) {
        this.classesOutputLocator = classesOutputLocator;
        return this;
    }

    /**
     * Sets the resources output directory.
     *
     * @param resourcesOutputDirectory The resources output directory.
     * @return This instance.
     */
    public TemplateGeneratorBuilder setResourcesOutputLocator(OutputLocator resourcesOutputLocator) {
        this.resourcesOutputLocator = resourcesOutputLocator;
        return this;
    }

    /**
     * Adds a new template suite generator to generate classes.
     *
     * @param generator The generator to add.
     * @return This instance.
     */
    public TemplateGeneratorBuilder addClassesTemplateSuiteGenerator(TemplateSuiteGenerator generator) {
        if (classesOutputLocator == null) {
            throw new NullPointerException(
                    "Classes output locator not specified, call 'setClassesOutputLocator' first");
        }
        templateSuiteGenerators.add(new TSGeneratorDirectoryPair(
                classesOutputLocator, generator));
        generatingClasses = true;
        return this;
    }

    /**
     * Adds a new template class generator to generate classes.
     *
     * @param generator The generator to add.
     * @return This instance.
     */
    public TemplateGeneratorBuilder addClassesTemplateClassGenerator(TemplateClassGenerator generator) {
        if (classesOutputLocator == null) {
            throw new NullPointerException(
                    "Classes output locator not specified, call 'setClassesOutputLocator' first");
        }
        templateClassGenerators.add(new TCGeneratorDirectoryPair(
                classesOutputLocator, generator));
        generatingClasses = true;
        return this;
    }

    /**
     * Adds a new template suite generator to generate resources.
     *
     * @param generator The generator to add.
     * @return This instance.
     */
    public TemplateGeneratorBuilder addResourcesTemplateSuiteGenerator(TemplateSuiteGenerator generator) {
        if (resourcesOutputLocator == null) {
            throw new NullPointerException(
                    "Resources output locator not specified, call 'setClassesOutputLocator' first");
        }
        templateSuiteGenerators.add(new TSGeneratorDirectoryPair(
                resourcesOutputLocator, generator));
        generatingResources = true;
        return this;
    }

    /**
     * Adds a new template class generator to generate resources.
     *
     * @param generator The generator to add.
     * @return This instance.
     */
    public TemplateGeneratorBuilder addResourcesTemplateClassGenerator(TemplateClassGenerator generator) {
        if (resourcesOutputLocator == null) {
            throw new NullPointerException(
                    "Resources output locator not specified, call 'setClassesOutputLocator' first");
        }
        templateClassGenerators.add(new TCGeneratorDirectoryPair(
        		resourcesOutputLocator, generator));
        generatingResources = true;
        return this;
    }

    /**
     * Builds and returns a new template generator.
     *
     * @return The new template generator.
     */
    public TemplateGenerator build() {
        return new BasicTemplateGenerator(templateSuiteGenerators,
                templateClassGenerators, generatingClasses, generatingResources);
    }

}

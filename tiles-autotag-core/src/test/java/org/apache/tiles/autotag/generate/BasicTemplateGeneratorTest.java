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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tiles.autotag.core.OutputLocator;
import org.apache.tiles.autotag.generate.BasicTemplateGenerator.TCGeneratorDirectoryPair;
import org.apache.tiles.autotag.generate.BasicTemplateGenerator.TSGeneratorDirectoryPair;
import org.apache.tiles.autotag.model.TemplateClass;
import org.apache.tiles.autotag.model.TemplateSuite;
import org.junit.Test;

/**
 * Tests {@link BasicTemplateGenerator}.
 *
 * @version $Rev$ $Date$
 */
public class BasicTemplateGeneratorTest {

    /**
     * Test method for {@link BasicTemplateGenerator#generate(String, TemplateSuite, Map)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testGenerate() throws IOException {
    	OutputLocator locator = createMock(OutputLocator.class);
        TemplateSuite suite = createMock(TemplateSuite.class);
        TemplateClass templateClass = createMock(TemplateClass.class);
        TemplateSuiteGenerator templateSuiteGenerator = createMock(TemplateSuiteGenerator.class);
        TemplateClassGenerator templateClassGenerator = createMock(TemplateClassGenerator.class);
        @SuppressWarnings("unchecked")
        Map<String, String> parameters = createMock(Map.class);
        List<TemplateClass> templateClasses = new ArrayList<TemplateClass>();

        templateClasses.add(templateClass);

        expect(suite.getTemplateClasses()).andReturn(templateClasses);
        templateSuiteGenerator.generate(locator, "my.package", suite, parameters);
        templateClassGenerator.generate(locator, "my.package", suite, templateClass, parameters, "my.Runtime", "my.Request");

        replay(suite, templateClass, templateSuiteGenerator, templateClassGenerator, parameters);
        TSGeneratorDirectoryPair tsPair = new TSGeneratorDirectoryPair(locator, templateSuiteGenerator);
        TCGeneratorDirectoryPair tcPair = new TCGeneratorDirectoryPair(locator, templateClassGenerator);
        List<TSGeneratorDirectoryPair> tsList = new ArrayList<BasicTemplateGenerator.TSGeneratorDirectoryPair>();
        tsList.add(tsPair);
        List<TCGeneratorDirectoryPair> tcList = new ArrayList<BasicTemplateGenerator.TCGeneratorDirectoryPair>();
        tcList.add(tcPair);
        BasicTemplateGenerator generator = new BasicTemplateGenerator(tsList, tcList, true, false);
        assertTrue(generator.isGeneratingClasses());
        assertFalse(generator.isGeneratingResources());
        generator.generate("my.package", suite, parameters, "my.Runtime", "my.Request");
        verify(suite, templateClass, templateSuiteGenerator, templateClassGenerator, parameters);
    }
}

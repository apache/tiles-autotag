/**
 *
 */
package org.apache.tiles.autotag.jsp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tiles.autotag.core.runtime.ModelBody;
import org.apache.tiles.autotag.model.TemplateClass;
import org.apache.tiles.autotag.model.TemplateMethod;
import org.apache.tiles.autotag.model.TemplateParameter;
import org.apache.tiles.autotag.model.TemplateSuite;
import org.apache.tiles.request.Request;
import org.apache.velocity.app.Velocity;
import org.junit.Test;

/**
 * Tests {@link TagClassGenerator}.
 *
 * @version $Rev$ $Date$
 */
public class TagClassGeneratorTest {

    /**
     * Test method for {@link TagClassGenerator#generate(File, String, TemplateSuite, TemplateClass)}.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void testGenerate() throws Exception {
        TagClassGenerator generator = new TagClassGenerator();
        File file = File.createTempFile("autotag", null);
        file.delete();
        file.mkdir();
        file.deleteOnExit();
        TemplateSuite suite = new TemplateSuite("tldtest", "Test for TLD docs.");
        suite.getCustomVariables().put("taglibURI", "http://www.initrode.net/tags/test");

        List<TemplateParameter> params = new ArrayList<TemplateParameter>();
        TemplateParameter param = new TemplateParameter("one", "java.lang.String", true);
        param.setDocumentation("Parameter one.");
        params.add(param);
        param = new TemplateParameter("two", "int", false);
        param.setDocumentation("Parameter two.");
        params.add(param);
        param = new TemplateParameter("three", "boolean", false);
        param.setDocumentation("Parameter three.");
        params.add(param);
        param = new TemplateParameter("request", Request.class.getName(), false);
        param.setDocumentation("The request.");
        params.add(param);
        param = new TemplateParameter("modelBody", ModelBody.class.getName(), false);
        param.setDocumentation("The body.");
        params.add(param);
        TemplateMethod executeMethod = new TemplateMethod("execute", params);

        TemplateClass clazz = new TemplateClass("org.apache.tiles.autotag.template.DoStuffTemplate",
                "doStuff", "DoStuff", executeMethod);
        clazz.setDocumentation("Documentation of the DoStuff class.");

        Properties props = new Properties();
        InputStream propsStream = getClass().getResourceAsStream("/org/apache/tiles/autotag/jsp/velocity.properties");
        props.load(propsStream);
        propsStream.close();
        Velocity.init(props);

        generator.generate(file, "org.apache.tiles.autotag.jsp.test", suite, clazz);

        InputStream expected = getClass().getResourceAsStream("/org/apache/tiles/autotag/jsp/test/DoStuffTag.java");
        File effectiveFile = new File(file, "/org/apache/tiles/autotag/jsp/test/DoStuffTag.java");
        assertTrue(effectiveFile.exists());
        InputStream effective = new FileInputStream(effectiveFile);
        assertTrue(IOUtils.contentEquals(effective, expected));
        effective.close();
        expected.close();

        suite.addTemplateClass(clazz);
        params = new ArrayList<TemplateParameter>();
        param = new TemplateParameter("one", "java.lang.Double", true);
        param.setDocumentation("Parameter one.");
        params.add(param);
        param = new TemplateParameter("two", "float", false);
        param.setDocumentation("Parameter two.");
        params.add(param);
        param = new TemplateParameter("three", "java.util.Date", false);
        param.setDocumentation("Parameter three.");
        params.add(param);
        param = new TemplateParameter("request", Request.class.getName(), false);
        param.setDocumentation("The request.");
        params.add(param);
        executeMethod = new TemplateMethod("execute", params);

        clazz = new TemplateClass("org.apache.tiles.autotag.template.DoStuffNoBodyTemplate",
                "doStuffNoBody", "DoStuffNoBody", executeMethod);
        clazz.setDocumentation("Documentation of the DoStuffNoBody class.");

        suite.addTemplateClass(clazz);

        generator.generate(file, "org.apache.tiles.autotag.jsp.test", suite, clazz);

        expected = getClass().getResourceAsStream("/org/apache/tiles/autotag/jsp/test/DoStuffNoBodyTag.java");
        effectiveFile = new File(file, "/org/apache/tiles/autotag/jsp/test/DoStuffNoBodyTag.java");
        assertTrue(effectiveFile.exists());
        effective = new FileInputStream(effectiveFile);
        assertTrue(IOUtils.contentEquals(effective, expected));
        effective.close();
        expected.close();

        FileUtils.deleteDirectory(file);
    }

}

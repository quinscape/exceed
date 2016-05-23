package de.quinscape.exceed.tooling;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.svenson.JSON;
import org.svenson.info.JavaObjectSupport;
import org.svenson.info.ObjectSupport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generates a list of module resources packaged with the exceed library
 *
 * @goal generatefilelist
 * @phase generate-resources
 */
public class GenerateModelDocs
{
    private final static Logger log = LoggerFactory.getLogger(GenerateModelDocs.class);

    private final static ObjectSupport javaSupport = new JavaObjectSupport();

    public static void main(String[] args) throws Exception
    {
        new GenerateModelDocs().main();
    }

    private Map<Class<?>, JavaDocs> docsMap = new HashMap<>();

    public void main() throws Exception
    {

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(Model.class));
        Set<BeanDefinition> candidates = provider.findCandidateComponents("de.quinscape.exceed.model");

        for (BeanDefinition definition : candidates)
        {
            Class<?> cls = Class.forName(definition.getBeanClassName());
            do
            {
                getDocs(cls);
            } while ((cls = cls.getSuperclass()) != null && !cls.equals(Object.class));
        }

        File out = new File("./target/classes/de/quinscape/exceed/model/model-docs.json");

        FileUtils.writeStringToFile(out, JSON.defaultJSON().forValue(docsMap), "UTF-8");
    }


    private JavaDocs getDocs(Class<?> declaringClass) throws IOException
    {
        JavaDocs javaDocs = docsMap.get(declaringClass);
        if (javaDocs == null)
        {
            javaDocs = readJavadocs(new File("./src/main/java/".replace('/', File.separatorChar)),declaringClass);
            docsMap.put(declaringClass, javaDocs);
        }

        return javaDocs;
    }


    public static File sourceFile(File base, Class<?> declaringClass)
    {
        return new File(base, declaringClass.getName().replace('.', File.separatorChar) + ".java");
    }
    public static JavaDocs readJavadocs(File base, Class<?> declaringClass) throws IOException
    {
        File source =  sourceFile(base, declaringClass);
        if (!source.exists())
        {
            throw new RuntimeException("Source " + source + " does not exist: pwd is " + new File(".").getAbsolutePath());
        }

        try
        {
            return new JavaDocs(source);
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new ExceedRuntimeException(e);
        }
        catch (InstantiationException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }
}

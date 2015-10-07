package de.quinscape.exceed.runtime.resource.classpath;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ExtensionResource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassPathResourceRoot
    implements ResourceRoot
{
    private static Logger log = LoggerFactory.getLogger(ClassPathResourceRoot.class);

    private final String classPathBase;
    private int extensionIndex;

    public ClassPathResourceRoot(String classPathBase)
    {
        this.classPathBase = classPathBase;
    }


    private List<String> readFileList()
    {
        // the build process has provided us with a list of all module resources packed in our jar
        try
        {
            String path = classPathBase + "/resource.lst";

            log.info("Reading file list from {}", path);

            InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
            return IOUtils.readLines(is, "UTF-8");
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    @Override
    public void setExtensionIndex(int extensionIndex)
    {
        this.extensionIndex = extensionIndex;
    }

    @Override
    public List<? extends ExtensionResource> listResources()
    {
        List<String> relativePaths = readFileList();

        log.debug("module resources: {}", relativePaths);

        List<ExtensionResource> list = new ArrayList<>();
        for (String relative : relativePaths)
        {
            String path = classPathBase + relative;
            list.add(new ClassPathExtensionResource(extensionIndex, this.getClass().getClassLoader(), path, relative));
        }

        return list;
    }

    @Override
    public boolean supportsHotReloading()
    {
        return false;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + classPathBase
            ;
    }
}

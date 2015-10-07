package de.quinscape.exceed.runtime.resource.classpath;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.Extension;
import de.quinscape.exceed.runtime.resource.file.FileExtensionResource;
import de.quinscape.exceed.runtime.resource.file.JSONFileFilter;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class ClassPathExtension
    implements Extension
{
    private static Logger log = LoggerFactory.getLogger(ClassPathExtension.class);

    private final String classPathBase;

    public ClassPathExtension(String classPathBase)
    {
        this.classPathBase = classPathBase;
    }

    @Override
    public void insertResources(ConcurrentMap<String, ResourceLocation> locations, int
        extensionIndex)
    {
        List<String> relativePaths = readFileList();

        log.debug("module resources: {}", relativePaths);

        for (String relative : relativePaths)
        {
            ResourceLocation location = locations.get(relative);

            String path = classPathBase + relative;

            if (location == null)
            {
                location = new ResourceLocation(relative);
                locations.put(relative, location);
            }

            location.addExtensionResource(new ClassPathExtensionResource(extensionIndex, this.getClass().getClassLoader(), path));
        }
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

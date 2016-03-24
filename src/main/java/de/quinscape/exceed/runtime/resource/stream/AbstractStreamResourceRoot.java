package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractStreamResourceRoot
    implements ResourceRoot
{
    private final static Logger log = LoggerFactory.getLogger(ClassPathResourceRoot.class);
    protected final String pathBase;
    private int extensionIndex;


    public AbstractStreamResourceRoot(String pathBase)
    {
        this.pathBase = pathBase;
    }


    private List<String> readFileList()
    {
        // the build process has provided us with a list of all module resources packed in our jar
        try
        {
            String path = pathBase + "/resource.lst";

            log.info("Reading file list from {}", path);

            InputStream is = openStream(path);

            if (is == null)
            {
                throw new IllegalStateException("Could not find resource list for " + this);
            }

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
    public int getExtensionIndex()
    {
        return extensionIndex;
    }


    @Override
    public List<? extends AppResource> listResources()
    {
        List<String> relativePaths = readFileList();

        log.debug("module resources: {}", relativePaths);

        return relativePaths.stream()
            .map(this::getResource)
            .collect(Collectors.toList());
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + pathBase;
    }


    protected abstract InputStream openStream(String path);
}

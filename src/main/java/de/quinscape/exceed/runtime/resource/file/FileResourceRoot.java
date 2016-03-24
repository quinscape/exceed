package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.watch.Java7NIOResourceWatcher;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileResourceRoot
    implements ResourceRoot
{
    private final static Logger log = LoggerFactory.getLogger(FileResourceRoot.class);

    private final File baseDirectory;
    private final Java7NIOResourceWatcher watcher;

    /**
     * Index of the extension this root belongs to in the list of extensions.
     */
    private int extensionIndex;


    public FileResourceRoot(File baseDirectory, boolean hotReload) throws IOException
    {
        this.baseDirectory = baseDirectory;
        if (hotReload)
        {
            log.debug("Registering resource watcher for {}", baseDirectory);

            watcher = new Java7NIOResourceWatcher(this);
            watcher.start();
        }
        else
        {
            watcher = null;
        }
    }


    public File getBaseDirectory()
    {
        return baseDirectory;
    }


    private List<ResourceChangeListener> listeners = new ArrayList<>();


    @Override
    public List<? extends AppResource> listResources()
    {
        int relativeFileNameStart = baseDirectory.getPath().length();

        List<AppResource> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(baseDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
        {
            String relative = file.getPath().substring(relativeFileNameStart);

            list.add(new FileAppResource(this, file, relative));
        }

        return list;
    }


    @Override
    public AppResource getResource(String path)
    {
        int relativeFileNameStart = baseDirectory.getPath().length();
        File file = new File(baseDirectory, Util.path(path));
        String relative = file.getPath().substring(relativeFileNameStart);
        return new FileAppResource(this, file, relative);
    }


    public void setExtensionIndex(int extensionIndex)
    {
        this.extensionIndex = extensionIndex;
    }


    public int getExtensionIndex()
    {
        return extensionIndex;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": " + baseDirectory
            + ", hotReload = " + (watcher != null)
            ;
    }


    @Override
    public ResourceWatcher getResourceWatcher()
    {
        return watcher;
    }
}

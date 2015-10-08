package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileResourceRoot
    implements ResourceRoot
{
    private final File baseDirectory;

    /**
     * Index of the extension this root belongs to in the list of extensions.
     */
    private int extensionIndex;

    public FileResourceRoot(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }

    public File getBaseDirectory()
    {
        return baseDirectory;
    }

    @Override
    public List<? extends AppResource> listResources()
    {
        int relativeFileNameStart = baseDirectory.getPath().length();

        List<AppResource> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(baseDirectory, JSONFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
        {
            String relative = file.getPath().substring(relativeFileNameStart);

            list.add(new FileAppResource(extensionIndex, file, relative));
        }

        return list;
    }

    @Override
    public boolean supportsHotReloading()
    {
        return true;
    }

    @Override
    public AppResource getResource(String path)
    {
        int relativeFileNameStart = baseDirectory.getPath().length();
        File file = new File(baseDirectory, Util.path(path));
        String relative = file.getPath().substring(relativeFileNameStart);
        return new FileAppResource(extensionIndex, file, relative);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + baseDirectory
            ;
    }

    public void setExtensionIndex(int extensionIndex)
    {
        this.extensionIndex = extensionIndex;
    }

    public int getExtensionIndex()
    {
        return extensionIndex;
    }
}

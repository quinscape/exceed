package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ExtensionResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileResourceRoot
    implements ResourceRoot
{
    private final File baseDirectory;

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
    public List<? extends ExtensionResource> listResources()
    {
        String extensionPath = baseDirectory.getPath();
        int relativeFileNameStart = extensionPath.length();

        List<ExtensionResource> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(baseDirectory, JSONFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
        {
            String relative = file.getPath().substring(relativeFileNameStart);

            list.add(new FileExtensionResource(extensionIndex, file, relative));
        }

        return list;
    }

    @Override
    public boolean supportsHotReloading()
    {
        return true;
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

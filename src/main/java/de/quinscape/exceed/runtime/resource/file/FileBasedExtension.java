package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.Extension;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class FileBasedExtension
    implements Extension
{
    private final File baseDirectory;

    public FileBasedExtension(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }

    public File getBaseDirectory()
    {
        return baseDirectory;
    }

    public static List<FileBasedExtension> fromDirs(File... files)
    {
        List<FileBasedExtension> extensions = new ArrayList<>();
        for (File file : files)
        {
            extensions.add(new FileBasedExtension(file));
        }

        return extensions;
    }

    @Override
    public void insertResources(ConcurrentMap<String, ResourceLocation> locations, int extensionIndex)
    {
        String extensionPath = baseDirectory.getPath();
        int relativeFileNameStart = extensionPath.length();

        for (File file : FileUtils.listFiles(baseDirectory, JSONFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
        {
            String relative = file.getPath().substring(relativeFileNameStart);

            ResourceLocation location = locations.get(relative);
            if (location == null)
            {
                location = new ResourceLocation(relative);
                locations.put(relative, location);
            }

            location.addExtensionResource(new FileExtensionResource(extensionIndex, file));
        }
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
}

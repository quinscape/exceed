package de.quinscape.exceed.runtime.resource.file;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

public class JSONFileFilter
    implements IOFileFilter
{
    public final static JSONFileFilter INSTANCE = new JSONFileFilter();

    private final static String JSON_EXTENSION = ".json";

    private JSONFileFilter()
    {

    }

    @Override
    public boolean accept(File file)
    {
        return file.getName().endsWith(JSON_EXTENSION);
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return name.endsWith(JSON_EXTENSION);
    }
}

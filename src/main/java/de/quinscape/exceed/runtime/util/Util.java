package de.quinscape.exceed.runtime.util;


import java.io.File;

public final class Util
{
    private Util()
    {

    }

    public static String path(String path)
    {
        char separatorChar = File.separatorChar;
        if (separatorChar != '/')
        {
            return path.replace('/', separatorChar);
        }
        return path;
    }
}

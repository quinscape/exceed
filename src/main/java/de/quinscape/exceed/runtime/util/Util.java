package de.quinscape.exceed.runtime.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Util
{
    public static final String LIBRARY_SOURCE_SYSTEM_PROPERTY = "exceed.library.source";
    private static Logger log = LoggerFactory.getLogger(Util.class);

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

    final static char[] BASE32_ALPHABET = "0123456789abcdefghijklmnopqrstuv".toCharArray();
    final static String C0 = String.valueOf(BASE32_ALPHABET[0]);

    public static String base32(long value)
    {
        StringBuilder sb = new StringBuilder();

        do
        {
            int lowerBits = (int) (value & 31);
            sb.append( BASE32_ALPHABET[lowerBits]);
            value >>>= 5;
        } while (value != 0);
        return sb.reverse().toString();
    }

    private static AtomicBoolean sourceChecked = new AtomicBoolean(false);

    public static File getExceedLibrarySource()
    {
        String property = System.getProperty(LIBRARY_SOURCE_SYSTEM_PROPERTY);
        if (property == null)
        {
            return null;
        }
        File sourceDir = new File(property);

        if (!sourceChecked.get())
        {
            if (!isValidSourceDir(sourceDir))
            {
                throw new IllegalStateException("System property " + LIBRARY_SOURCE_SYSTEM_PROPERTY + " is set but does not point to a valid source checkout");
            }
            sourceChecked.set(true);
        }

        return sourceDir;
    }

    private static boolean isValidSourceDir(File sourceDir)
    {
        return sourceDir.isDirectory() && new File(sourceDir, path("src/main/java/de/quinscape/exceed/runtime/ExceedApplicationConfiguration.java")).isFile();
    }

    public static String parentDir(String relativePath)
    {
        int pos = relativePath.lastIndexOf('/');
        if (pos < 0)
        {
            return "";
        }
        return relativePath.substring(0, pos);
    }
}

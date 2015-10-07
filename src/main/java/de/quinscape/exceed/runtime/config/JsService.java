package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.controller.ScriptController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

public class JsService
{
    public static final String SOURCE_PATH = "/exceed/js/main.js";
    public static final String MAP_PATH = "/exceed/js/main.js.map";
    private static Logger log = LoggerFactory.getLogger(ScriptController.class);

    private volatile long sourceModified;
    private final String sourceLocation;
    private String source;
    private byte[] sourceData;
    private byte[] mapData;

    public JsService(ServletContext servletContext, String sourceLocation) throws IOException
    {
        if (sourceLocation != null)
        {
            this.sourceLocation = sourceLocation;
            loadSource();
        }
        else
        {
            this.sourceLocation = null;

            log.info("Loading js sources from classpath");

            log.info("paths: {}", servletContext.getResourcePaths("/").stream().filter( p -> !p.toString().contains(".jar")));

            InputStream sourceStream = servletContext.getResourceAsStream(SOURCE_PATH);
            InputStream mapStream = servletContext.getResourceAsStream(MAP_PATH);

            if (sourceStream == null)
            {
                throw new ExceedRuntimeException("Cannot read source from classpath resource " + SOURCE_PATH);
            }

            if (mapStream == null)
            {
                throw new ExceedRuntimeException("Cannot read source map from classpath resource " + MAP_PATH);
            }

            this.source = IOUtils.toString(sourceStream, "UTF-8");
            this.sourceData = this.source.getBytes("UTF-8");
            this.mapData = IOUtils.toString(mapStream, "UTF-8").getBytes("UTF-8");
        }
    }

    private void loadSource()
    {
        if (sourceLocation == null)
        {
            return;
        }

        try
        {
            File sourceFile = new File(sourceLocation);

            if (!sourceFile.exists() || !sourceFile.isFile())
            {
                throw new IllegalStateException(sourceFile + " is no existing source file.");
            }

            File mapFile = new File(sourceLocation + ".map");
            long sourceModified = sourceFile.lastModified();
            if (sourceModified > this.sourceModified)
            {
                synchronized (this)
                {
                    if (sourceModified > this.sourceModified)
                    {
                        log.info("{} js sources from {}", this.sourceModified == 0l ? "Loading" : "Reloading", sourceFile);

                        this.source = FileUtils.readFileToString(sourceFile, "UTF-8");
                        this.sourceData = this.source.getBytes("UTF-8");
                        this.mapData = FileUtils.readFileToString(mapFile, "UTF-8").getBytes("UTF-8");
                        this.sourceModified = sourceModified;
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException("Error loading js source", e);
        }
    }

    public String getSource()
    {
        loadSource();
        return source;
    }

    public byte[] getSourceData()
    {
        loadSource();
        return sourceData;
    }

    public byte[] getMapData()
    {
        loadSource();
        return mapData;
    }
}

package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.controller.ScriptController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class JsService
{
    private static Logger log = LoggerFactory.getLogger(ScriptController.class);

    private volatile long sourceModified;
    private final String sourceLocation;
    private String source;
    private byte[] sourceData;
    private byte[] mapData;

    public JsService(String sourceLocation) throws IOException
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

            this.source = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream
                ("/de/quinscape/exceed/resources/js/exceed/main.js"), "UTF-8");
            this.sourceData = this.source.getBytes("UTF-8");
            this.mapData = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream
                ("/de/quinscape/exceed/resources/js/exceed/main.js.map"), "UTF-8").getBytes("UTF-8");
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

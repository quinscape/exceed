package de.quinscape.exceed.model.meta;

import org.svenson.JSONParameter;
import org.svenson.JSONParameters;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents one generated webpack Javascript entry point file.
 */
public class WebpackEntryPoint
{
    private final String name;
    private final int size;
    private final List<Integer> chunks;
    private final List<String> chunkNames;
    private final Map<String,Object> attributes;


    public WebpackEntryPoint(
        @JSONParameter("name")
        String name,

        @JSONParameter("size")
        int size,

        @JSONParameter("chunks")
        List<Integer> chunks,

        @JSONParameter("chunkNames")
        List<String> chunkNames,

        @JSONParameters
        Map<String, Object> attributes)
    {
        this.name = name;
        this.size = size;
        this.chunks = chunks;
        this.chunkNames = chunkNames;
        this.attributes = Collections.unmodifiableMap(attributes);
    }


    /**
     * Returns the name of the file.
     *
     * @return name of the file
     */
    public String getName()
    {
        return name;
    }


    /**
     * Returns the size of the file.
     *
     * @return size of the file
     */
    public int getSize()
    {
        return size;
    }


    /**
     * List of chunk indizes contained in this entry.
     *
     * @return  list of chunk indizes
     */
    public List<Integer> getChunks()
    {
        return chunks;
    }


    /**
     * List of chunk names contained in this file.
     *
     * @return list of chunk names.
     */
    public List<String> getChunkNames()
    {
        return chunkNames;
    }


    public Map<String, Object> getAttributes()
    {
        return attributes;
    }
}

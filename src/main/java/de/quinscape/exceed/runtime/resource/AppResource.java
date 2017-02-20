package de.quinscape.exceed.runtime.resource;

/**
 * Encapsulates a model resource within an extension
 */
public interface AppResource
{
    /**
     * Returns the resource root this resource belongs to.
     *
     * @return resource root
     */
    ResourceRoot getResourceRoot();

    /**
     * Reads the contents of the resource.
     *
     * @return contents
     */
    byte[] read();

    /**
     * Returns a last modified value for the resource, if applicable.
     *
     * @return last modified timestamp
     */
    long lastModified();

    boolean exists();

    String getRelativePath();

    default boolean isWritable()
    {
        return getResourceRoot().isWritable();
    }

    boolean delete();

    void write(byte[] bytes);
}

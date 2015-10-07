package de.quinscape.exceed.runtime.resource;

/**
 * Encapsulates a model resource within an extension
 */
public interface ExtensionResource
{
    /**
     * Returns the position of the parent index within the system's active extensions.
     * @return position
     */
    int getExtensionIndex();

    /**
     * Reads the contents of the resource.
     *
     * @return contents
     */
    String read();

    /**
     * Returns a last modified value for the resource, if applicable.
     *
     * @return last modified timestamp
     */
    long lastModified();
}

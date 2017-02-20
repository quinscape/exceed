package de.quinscape.exceed.runtime.resource;

import java.util.List;

/**
 * Encapsulates a tree of resources accessible via the internal resource abstraction API used for loading models
 * and resources.
 *
 * An application will normally have multiple resource roots some or all of which may be writable. For production
 * executions, usually none of the resource roots will be writable.
 *
 * We might have base resources being loaded from a JAR (non-writable for our purposes) and another file resource root
 * that contains the model objects for the local application.
 *
 */
public interface ResourceRoot
{
    void setExtensionIndex(int extensionIndex);

    int getExtensionIndex();

    List<? extends AppResource> listResources();

    AppResource getResource(String path);

    ResourceWatcher getResourceWatcher();

    String getName();

    boolean isWritable();
}

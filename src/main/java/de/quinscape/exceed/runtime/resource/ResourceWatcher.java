package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.watch.Java7NIOResourceWatcher;

public interface ResourceWatcher
{
    void register(ResourceChangeListener listener);

    void clearListeners();
}

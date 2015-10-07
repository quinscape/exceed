package de.quinscape.exceed.runtime.resource.file.watch;

/**
 * Created by sven on 20.10.14.
 */
public interface ModuleWatcher
{
    void register(ModuleWatcherListener listener);

    void clearListeners();
}

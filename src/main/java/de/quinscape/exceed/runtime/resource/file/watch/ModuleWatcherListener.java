package de.quinscape.exceed.runtime.resource.file.watch;


import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;

/**
 * Implemented by classes wanting to listen to module resource events.
 *
 * @see Java7NIOModuleWatcher#register(ModuleWatcherListener)
 */
public interface ModuleWatcherListener
{
    String getListenerBase();

    void onModuleChange(ModuleResourceEvent event, String modulePath);
}


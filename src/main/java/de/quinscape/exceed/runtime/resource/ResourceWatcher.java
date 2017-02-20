package de.quinscape.exceed.runtime.resource;

public interface ResourceWatcher
{
    void register(ResourceChangeListener listener);

    void clearListeners();

    void enable();

    void disable();
}

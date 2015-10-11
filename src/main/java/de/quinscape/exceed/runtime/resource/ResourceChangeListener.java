package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;

public interface ResourceChangeListener
{
    void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String resourcePath);
}

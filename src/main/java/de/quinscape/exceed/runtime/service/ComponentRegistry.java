package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.component.ComponentRegistration;

import java.util.Set;

public interface ComponentRegistry
{
    ComponentRegistration getComponentRegistration(String name);

    Set<String> getComponentNames();
    
    String COMPONENT_PACKAGE_FILE_NAME = "components.json";
}

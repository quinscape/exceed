package de.quinscape.exceed.runtime.service;

import java.util.Set;

public interface ComponentRegistry
{
    ComponentRegistration getComponentRegistration(String name);

    Set<String> getComponentNames();
}

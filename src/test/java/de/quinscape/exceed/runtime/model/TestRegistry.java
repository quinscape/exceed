package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;

import java.util.HashMap;
import java.util.Map;

public class TestRegistry
    implements ComponentRegistry
{
    private final Map<String, ComponentRegistration> componentRegistrations;


    public TestRegistry(Map<String, ComponentDescriptor> componentDescriptors)
    {
        if (componentDescriptors == null)
        {
            throw new IllegalArgumentException("componentDescriptors can't be null");
        }

        Map<String, ComponentRegistration> m = new HashMap<>();
        for (Map.Entry<String, ComponentDescriptor> entry : componentDescriptors.entrySet())
        {
            String componentName = entry.getKey();
            m.put(componentName, new ComponentRegistration(componentName, entry.getValue(), "", null));
        }
        this.componentRegistrations = m;
    }


    @Override
    public ComponentRegistration getComponentRegistration(String name)
    {
        return componentRegistrations.get(name);
    }
}

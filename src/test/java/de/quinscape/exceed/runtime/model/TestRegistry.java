package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.ComponentPackageDescriptor;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            m.put(componentName, new ComponentRegistration(componentName, entry.getValue(), "", null, null));
        }
        this.componentRegistrations = m;
    }

    public static TestRegistry loadComponentPackages(String... componentNames) throws FileNotFoundException
    {
        Map<String, ComponentDescriptor> map = new HashMap<>();

        List<String> names = new ArrayList<>();
        Collections.addAll(names, componentNames);
        names.add("std/common");

        for (String componentName : names)
        {
            ComponentPackageDescriptor pkg = JSONParser.defaultJSONParser().parse(ComponentPackageDescriptor.class, new InputStreamSource(new FileInputStream(new File("./src/main/js/components/" + componentName + "/components.json")), true));
            map.putAll(pkg.getComponents());
        }

        return new TestRegistry(map);
    }

    @Override
    public ComponentRegistration getComponentRegistration(String name)
    {
        return componentRegistrations.get(name);
    }


    @Override
    public Set<String> getComponentNames()
    {
        return componentRegistrations.keySet();
    }
}

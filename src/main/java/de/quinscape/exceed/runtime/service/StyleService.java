package de.quinscape.exceed.runtime.service;

import de.quinscape.dss.DSSConfig;
import de.quinscape.dss.ValueFactory;
import de.quinscape.dss.runtime.DynamicStylesheetsChassis;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Integrates dynamic DSS stylesheets into exceed and its resource concept.
 */
@Service
public class StyleService
{
    private final ExceedDSSResourceManager resourceManager;
    private final DynamicStylesheetsChassis chassis;

    public StyleService()
    {
        DSSConfig dssConfig = new DSSConfig();
        dssConfig.setDevelopment(true);
        dssConfig.setFunctionExecutor(new SpringContextFunctionExecutor());
        dssConfig.setValueFactory(new ValueFactory());

        resourceManager = new ExceedDSSResourceManager();

        dssConfig.setResourceManager(resourceManager);
        this.chassis = new DynamicStylesheetsChassis(dssConfig);
    }

    /**
     * Processes the given stylesheet from the give resource root
     *
     * @param root      resource root
     * @param path      path
     *
     * @return  processed stylesheet contents
     * @throws IOException
     */
    public synchronized String process(ResourceRoot root, String path) throws IOException
    {
        resourceManager.setResourceRoot(root);
        return chassis.process(path);
    }
}

package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelLoadingException;
import de.quinscape.exceed.runtime.resource.ApplicationResources;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ApplicationCompositionService
{
    private static Logger log = LoggerFactory.getLogger(ApplicationCompositionService.class);

    @Autowired
    private ModelJSONService modelJSONService;

    public ApplicationModel compose(ApplicationResources applicationResources)
    {

        ApplicationModel applicationModel = new ApplicationModel();

        Map<String, ResourceLocation> resourceLocations = applicationResources.getResourceLocations();
        for (Map.Entry<String, ResourceLocation> entry : resourceLocations.entrySet())
        {

            String path = entry.getKey();
            ResourceLocation resource = entry.getValue();
            update(applicationModel, path, resource);
        }

        return applicationModel;
    }

    public void update(ApplicationModel applicationModel, String path, ResourceLocation resource)
    {
        String json = resource.getHighestPriorityResource().read();

        try
        {
            if (path.equals("routing.json"))
            {
                RoutingTable routingTable = modelJSONService.toModel(RoutingTable.class, json);
                applicationModel.setRoutingTable(routingTable);
            }
            else if (path.startsWith("/domain/"))
            {
                if (path.startsWith("/domain/property"))
                {
                    PropertyType propertyType = modelJSONService.toModel(PropertyType.class, json);
                    applicationModel.getPropertyTypes().put(propertyType.getName(), propertyType);
                }
                else
                {
                    DomainType domainType = modelJSONService.toModel(DomainType.class, json);
                    applicationModel.getDomainTypes().put(domainType.getName(), domainType);
                }
            }
            else if (path.startsWith("/view/"))
            {
                View view = modelJSONService.toModel(View.class, json);
                applicationModel.getViews().put(view.getName(), view);
            }
            else
            {
                log.warn("Unknown resource {} at path {}", resource, path);
            }
        }
        catch (Exception e)
        {
            throw new ModelLoadingException("Error loading model from " + resource, e);
        }
    }
}

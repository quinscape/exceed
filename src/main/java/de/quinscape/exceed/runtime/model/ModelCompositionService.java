package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Layout;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.VersionedModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

@Service
public class ModelCompositionService
{
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String APP_MODEL_NAME = "/models/app.json";

    public static final String ROUTING_MODEL_NAME = "/models/routing.json";

    public static final String DOMAIN_MODEL_PREFIX = "/models/domain/";

    public static final String DOMAIN_PROPERTY_MODEL_PREFIX = "/models/domain/property";

    public static final String ENUM_MODEL_PREFIX = "/models/domain/enum/";

    public static final String VIEW_MODEL_PREFIX = "/models/view/";

    public static final String DOMAIN_LAYOUT_NAME = "/layout/domain.json";

    private static Logger log = LoggerFactory.getLogger(ModelCompositionService.class);

    private ModelJSONService modelJSONService;

    @Autowired
    public void setModelJSONService(ModelJSONService modelJSONService)
    {
        this.modelJSONService = modelJSONService;
    }

    @Autowired
    private ComponentRegistry componentRegistry;

    public void compose(RuntimeApplication runtimeApplication, Map<String, ResourceLocation> resourceLocations,
                        ApplicationModel applicationModel)
    {
        for (ResourceLocation resource : resourceLocations.values())
        {
            if (resource.getRelativePath().endsWith(FileExtension.JSON))
            {
                updateInternal(runtimeApplication, applicationModel, resource.getHighestPriorityResource());
            }
        }
    }


    public TopLevelModel update(RuntimeApplication runtimeApplication, ApplicationModel applicationModel, AppResource
        resource)
    {
        String path = resource.getRelativePath();

        if (!path.endsWith(FileExtension.JSON))
        {
            throw new IllegalArgumentException(resource + " is not a JSON resource");
        }

        return updateInternal(runtimeApplication, applicationModel, resource);
    }


    private TopLevelModel updateInternal(RuntimeApplication runtimeApplication, ApplicationModel applicationModel,
                                         AppResource resource)
    {
        String path = resource.getRelativePath();
        String json = new String(resource.read(), UTF8);
        try
        {
            if (path.equals(APP_MODEL_NAME))
            {
                ApplicationModel newAppModel = create(ApplicationModel.class, json, APP_MODEL_NAME);
                applicationModel.merge(newAppModel);
                return applicationModel;
            }
            else if (path.equals(ROUTING_MODEL_NAME))
            {
                log.debug("Reading {} as RoutingTable", path);

                RoutingTable routingTable = create(RoutingTable.class, json, ROUTING_MODEL_NAME);

                applicationModel.setRoutingTable(routingTable);
                return routingTable;
            }
            else if (path.startsWith(DOMAIN_MODEL_PREFIX))
            {
                if (path.startsWith(DOMAIN_PROPERTY_MODEL_PREFIX))
                {
                    log.debug("Reading {} as PropertyType", path);

                    PropertyType propertyType = create(PropertyType.class, json, path);
                    applicationModel.getPropertyTypes().put(propertyType.getName(), propertyType);
                    return propertyType;
                }
                if (path.startsWith(ENUM_MODEL_PREFIX))
                {
                    EnumModel enumModel = create(EnumModel.class, json, path);
                    applicationModel.getEnums().put(enumModel.getName(), enumModel);
                    return enumModel;
                }
                else
                {
                    log.debug("Reading {} as DomainType", path);

                    DomainType domainType = create(DomainType.class, json, path);
                    domainType.setDomainService(runtimeApplication.getDomainService());
                    applicationModel.getDomainTypes().put(domainType.getName(), domainType);
                    return domainType;
                }
            }
            else if (path.startsWith(VIEW_MODEL_PREFIX))
            {
                log.debug("Reading {} as View", path);
                View view = createViewModel(path, json, false);

                applicationModel.getViews().put(view.getName(), view);
                return view;
            }
            else if (path.equals(DOMAIN_LAYOUT_NAME))
            {
                log.debug("Reading {} as Domain Layout", path);
                Layout layout = create(Layout.class, json, path);

                applicationModel.setDomainLayout(layout);
                return layout;

            }
            else
            {
                log.warn("Unknown resource {} at path {}", resource, path);
                return null;
            }
        }
        catch (Exception e)
        {
            throw new ModelLoadingException("Error loading model from " + resource, e);
        }
    }


    public View createViewModel(String path, String json, boolean preview)
    {
        View view = create(View.class, json, path);
        ComponentUtil.updateComponentRegsAndParents(componentRegistry, view, null);
        view.setPreview(preview);
        view.setCachedJSON(modelJSONService.toJSON(view, JSONFormat.CLIENT));
        return view;
    }


    private <M extends Model> M create(Class<M> cls, String json, String path)
    {
        String nameFromPath = nameFromPath(path);

        M m = modelJSONService.toModel(cls, json);

        if (m instanceof TopLevelModel)
        {
            TopLevelModel namedModel = (TopLevelModel) m;
            namedModel.setFilename(nameFromPath + FileExtension.JSON);

            if (namedModel.getName() == null)
            {
                namedModel.setName(nameFromPath);
            }
        }
        if (m instanceof VersionedModel)
        {
            ((VersionedModel) m).setVersion(UUID.randomUUID().toString());
        }

        return m;
    }


    private String nameFromPath(String path)
    {
        int start = path.lastIndexOf('/');
        int end = path.lastIndexOf('.');
        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = path.length();
        }
        return path.substring(start + 1, end);
    }

}

package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Application;
import de.quinscape.exceed.model.Layout;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.util.FileExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

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

    @Autowired
    private ModelJSONService modelJSONService;

    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private QueryTransformer queryTransformer;


    public void compose(RuntimeApplication runtimeApplication, Map<String, ResourceLocation> resourceLocations,
                        Application applicationModel)
    {

        for (ResourceLocation resource : resourceLocations.values())
        {
            update(runtimeApplication, applicationModel, resource.getHighestPriorityResource());
        }
    }


    public TopLevelModel update(RuntimeApplication runtimeApplication, Application applicationModel, AppResource
        resource)
    {
        String path = resource.getRelativePath();
        if (path.endsWith(FileExtension.CSS))
        {
            return StyleChange.INSTANCE;
        }

        if (path.endsWith(FileExtension.JS))
        {
            return CodeChange.INSTANCE;
        }

        if (!path.endsWith(FileExtension.JSON))
        {
            return null;
        }

        String json = new String(resource.read(), UTF8);
        try
        {
            if (path.equals(APP_MODEL_NAME))
            {
                Application newAppModel = create(Application.class, json, APP_MODEL_NAME);
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
                View view = create(View.class, json, path);
                updateComponentRegistrations(runtimeApplication, view.getRoot(), null);
                view.setCachedJSON(modelJSONService.toJSON(view));

                applicationModel.getViews().put(view.getName(), view);
                return view;
            }
            else if (path.equals(DOMAIN_LAYOUT_NAME))
            {
                log.debug("Reading {} as Domain Layout", path);
                Layout view = create(Layout.class, json, path);

                applicationModel.setDomainLayout(view);
                return view;

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


    /**
     * Updates the application component registration for the given component model and its children
     *
     * @param runtimeApplication runtime application
     * @param elem               component model
     * @param componentNames     component names to update or <code>null</code> or empty to update all components.
     */
    public void updateComponentRegistrations(RuntimeApplication runtimeApplication, ComponentModel elem, Set<String>
        componentNames)
    {
        if (componentNames != null && componentNames.size() == 0)
        {
            componentNames = null;
        }
        updateComponentRegistrationsRecursive(runtimeApplication, elem, componentNames);
    }

    private void updateComponentRegistrationsRecursive(RuntimeApplication runtimeApplication, ComponentModel elem, Set<String>
        componentNames)
    {
        if (elem.isComponent() && (componentNames == null || componentNames.contains(elem.getName())))
        {
            ComponentRegistration registration = componentRegistry.getComponentRegistration(elem.getName());

            if (registration == null)
            {
                throw new IllegalStateException("No component registered for name '" + elem.getName() + "'");
            }

            if (registration.getDataProvider() != null && elem.getComponentId() == null)
            {
                throw new ModelCompositionException(elem + " must have a id attribute");
            }
            elem.setComponentRegistration(registration);
        }

        for (ComponentModel componentModel : elem.children())
        {
            updateComponentRegistrationsRecursive(runtimeApplication, componentModel, componentNames);
        }
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

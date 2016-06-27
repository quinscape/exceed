package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Layout;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static final String PROCESS_MODEL_PREFIX = "/models/process/";

    public static final String DOMAIN_LAYOUT_NAME = "/layout/domain.json";

    private final static Logger log = LoggerFactory.getLogger(ModelCompositionService.class);


    private ModelJSONService modelJSONService;

    @Autowired
    public void setModelJSONService(ModelJSONService modelJSONService)
    {
        this.modelJSONService = modelJSONService;
    }

    @Autowired
    private ComponentRegistry componentRegistry;

    public void compose(RuntimeApplication runtimeApplication, Map<String, ResourceLocation> resourceLocations,
                        ApplicationModel applicationModel, DomainService domainService)
    {
        for (ResourceLocation resource : resourceLocations.values())
        {
            if (resource.getRelativePath().endsWith(FileExtension.JSON))
            {
                updateInternal(runtimeApplication, applicationModel, resource.getHighestPriorityResource(), domainService);
            }
        }
    }


    public TopLevelModel update(RuntimeApplication runtimeApplication, ApplicationModel applicationModel, AppResource
        resource, DomainService domainService)
    {
        String path = resource.getRelativePath();

        if (!path.endsWith(FileExtension.JSON))
        {
            throw new IllegalArgumentException(resource + " is not a JSON resource");
        }

        TopLevelModel topLevelModel = updateInternal(runtimeApplication, applicationModel, resource, domainService);

        if (topLevelModel instanceof View)
        {
            postProcessView(runtimeApplication, (View) topLevelModel);
        }

        return topLevelModel;
    }


    private TopLevelModel updateInternal(RuntimeApplication runtimeApplication, ApplicationModel applicationModel,
                                         AppResource resource, DomainService domainService)
    {
        String path = resource.getRelativePath();
        String json = new String(resource.read(), UTF8);

        if (json.length() == 0)
        {
            return null;
        }

        try
        {
            if (path.equals(APP_MODEL_NAME))
            {
                ApplicationModel newAppModel = create(ApplicationModel.class, json, resource);
                applicationModel.merge(newAppModel);
                return applicationModel;
            }
            else if (path.equals(ROUTING_MODEL_NAME))
            {
                log.debug("Reading {} as RoutingTable", path);

                RoutingTable routingTable = create(RoutingTable.class, json, resource);

                applicationModel.setRoutingTable(routingTable);
                return routingTable;
            }
            else if (path.startsWith(DOMAIN_MODEL_PREFIX))
            {
                if (path.startsWith(DOMAIN_PROPERTY_MODEL_PREFIX))
                {
                    log.debug("Reading {} as PropertyType", path);

                    PropertyType propertyType = create(PropertyType.class, json, resource);
                    applicationModel.addPropertyType(propertyType.getName(), propertyType);
                    return propertyType;
                }
                if (path.startsWith(ENUM_MODEL_PREFIX))
                {
                    EnumType enumType = create(EnumType.class, json, resource);
                    applicationModel.addEnum(enumType.getName(), enumType);
                    return enumType;
                }
                else
                {
                    log.debug("Reading {} as DomainType", path);

                    DomainType domainType = create(DomainType.class, json, resource);
                    domainType.setDomainService(domainService);
                    applicationModel.addDomainType(domainType.getName(), domainType);
                    return domainType;
                }
            }
            else if (path.startsWith(VIEW_MODEL_PREFIX))
            {

                log.debug("Reading {} as View", path);
                View view = createViewModel(runtimeApplication, resource, json, false);

                applicationModel.addView(view.getName(), view);
                return view;
            }

            else if (path.startsWith(PROCESS_MODEL_PREFIX))
            {
                int nameStart = PROCESS_MODEL_PREFIX.length();
                String processName = path.substring(nameStart, path.indexOf('/', nameStart));

                if (path.contains("/view/"))
                {
                    log.debug("Reading {} as Process View", path);

                    View view = createViewModel(runtimeApplication, resource, json, false);
                    view.setProcessName(processName);

                    applicationModel.addView(view.getName(), view);
                    return view;
                }

                log.debug("Reading {} as Process", path);
                Process process = create(Process.class, json , resource);
                process.setName(processName);
                applicationModel.addProcess( process.getName(), process);
                return process;
            }
            else if (path.equals(DOMAIN_LAYOUT_NAME))
            {
                log.debug("Reading {} as Domain Layout", path);
                Layout layout = create(Layout.class, json, resource);

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


    public View createViewModel(RuntimeApplication runtimeApplication, AppResource resource, String json, boolean preview)
    {
        View view = create(View.class, json, resource);
        ComponentUtil.updateComponentRegsAndParents(componentRegistry, view, null);
        view.setPreview(preview);

        return view;
    }

    private final static Pattern PROCESS_PATH = Pattern.compile("^/models/process/(.*)/view/");

    private <M extends Model> M create(Class<M> cls, String json, AppResource resource)
    {

        String path = resource.getRelativePath();

        M model = modelJSONService.toModel(cls, json);


        if (model instanceof TopLevelModel)
        {
            TopLevelModel namedModel = (TopLevelModel) model;
            namedModel.setResource(resource);

            String name = nameFromPath(path);
            String processName;
            if (model instanceof View)
            {
                Matcher m = PROCESS_PATH.matcher(path);
                if (m.find())
                {
                    namedModel.setName(Process.getProcessViewName( m.group(1), name));
                }
                else
                {
                    namedModel.setName(name);
                }
            }
            else
            {
                namedModel.setName(name);
            }
        }
        if (model instanceof AutoVersionedModel)
        {
            ((AutoVersionedModel) model).setVersion(UUID.randomUUID().toString());
        }

        return model;
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


    /**
     * Called at the end of the runtime application initialization process.
     *
     * Allows models to be initialized after all models have been read.
     *
     * @param application
     * @param applicationModel
     */
    public void postProcess(RuntimeApplication application, ApplicationModel applicationModel)
    {
        for (View view : applicationModel.getViews().values())
        {
            postProcessView(application, view);
        }

        validateProcesses(applicationModel);
    }


    private void validateProcesses(ApplicationModel applicationModel)
    {
        for (Process process : applicationModel.getProcesses().values())
        {
            Transition startTransition = process.getStartTransition();
            if (startTransition == null)
            {
                throw new IllegalStateException("Process '" + process.getName() + "' defines no start transition");
            }

            ProcessState processState = process.getStates().get(startTransition.getTo());
            if (processState == null)
            {
                throw new IllegalStateException("Process '" + process.getName() + "' defines a non existing start-state: '" + startTransition + "' does not exist");
            }

            for (ProcessState state : process.getStates().values())
            {
                if (state instanceof ViewState)
                {
                    View view = process.getView(state.getName());
                    if (view == null)
                    {
                        throw new IllegalStateException("Process '" + process.getName() + "' defines a view state '" + state +"' but no such view model exists");
                    }
                }
            }
        }
    }


    public void postProcessView(RuntimeApplication application, View view)
    {
        view.initContext();
        view.setCachedJSON(modelJSONService.toJSON(application, view, JSONFormat.CLIENT));
    }


    public ModelJSONService getModelJSONService()
    {
        return modelJSONService;
    }
}

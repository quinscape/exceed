package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Application;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.util.FileExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Map;

@Service
public class ModelCompositionService
    implements ApplicationContextAware
{
    private static final String DEFAULT_DATA_PROVIDER = "defaultDataProvider";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String APP_MODEL_NAME = "/models/app.json";
    public static final String ROUTING_MODEL_NAME = "/models/routing.json";
    public static final String DOMAIN_MODEL_PREFIX = "/models/domain/";
    public static final String DOMAIN_PROPERTY_MODEL_PREFIX = "/models/domain/property";
    public static final String VIEW_MODEL_PREFIX = "/models/view/";

    private static Logger log = LoggerFactory.getLogger(ModelCompositionService.class);

    @Autowired
    private ModelJSONService modelJSONService;

    private ApplicationContext applicationContext;

    public Application compose(Map<String, ResourceLocation> resourceLocations)
    {
        Application applicationModel = new Application();

        for (ResourceLocation resource : resourceLocations.values())
        {
            update(applicationModel, resource.getHighestPriorityResource());
        }

        return applicationModel;
    }

    public TopLevelModel update(Application applicationModel, AppResource resource)
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
                else
                {
                    log.debug("Reading {} as DomainType", path);

                    DomainType domainType = create(DomainType.class, json, path);
                    applicationModel.getDomainTypes().put(domainType.getName(), domainType);
                    return domainType;
                }
            }
            else if (path.startsWith(VIEW_MODEL_PREFIX))
            {
                log.debug("Reading {} as View", path);

                View view = create(View.class, json, path);
                initializeDataProvider(view.getRoot());
                view.setCachedJSON(modelJSONService.toJSON(view));
                applicationModel.getViews().put(view.getName(), view);
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

    private void initializeDataProvider(ComponentModel elem)
    {
        String dataProvider = elem.getDataProvider();

        if (dataProvider == null)
        {
            dataProvider = DEFAULT_DATA_PROVIDER;
        }

        DataProvider dataProviderBean = null;
        try
        {
            Object bean = applicationContext.getBean(dataProvider);
            if (bean instanceof DataProvider)
            {
                dataProviderBean = (DataProvider) bean;
            }
            else
            {
                log.warn("Bean with name {} ( {} ) is no data provider", dataProvider, bean);
            }

        }
        catch(NoSuchBeanDefinitionException e)
        {
            log.warn("DataProvider {} is not defined.", dataProvider);
        }
        catch(BeansException e)
        {
            log.warn("Error creating DataProvider bean " + dataProvider, e);
        }

        if (dataProviderBean != null)
        {
            elem.setDataProviderInstance(dataProviderBean);
        }
        else
        {
            log.warn("Ignoring data provider {} on element {} due to errors", dataProvider, elem);
        }

        elem.children().forEach(this::initializeDataProvider);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}

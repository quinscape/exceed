package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.view.DataProvider;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.resource.ApplicationResources;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ModelCompositionService
    implements ApplicationContextAware
{
    private static final String DEFAULT_DATA_PROVIDER = "defaultDataProvider";

    private static Logger log = LoggerFactory.getLogger(ModelCompositionService.class);

    @Autowired
    private ModelJSONService modelJSONService;

    private ApplicationContext applicationContext;

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
            if (path.equals("/routing.json"))
            {
                log.debug("Reading {} as RoutingTable", path);

                RoutingTable routingTable = create(RoutingTable.class, json, path);

                applicationModel.setRoutingTable(routingTable);
            }
            else if (path.startsWith("/domain/"))
            {
                if (path.startsWith("/domain/property"))
                {
                    log.debug("Reading {} as PropertyType", path);

                    PropertyType propertyType = create(PropertyType.class, json, path);
                    applicationModel.getPropertyTypes().put(propertyType.getName(), propertyType);
                }
                else
                {
                    log.debug("Reading {} as DomainType", path);

                    DomainType domainType = create(DomainType.class, json, path);
                    applicationModel.getDomainTypes().put(domainType.getName(), domainType);
                }
            }
            else if (path.startsWith("/view/"))
            {
                log.debug("Reading {} as View", path);

                View view = create(View.class, json, path);

                initializeDataProvider(view.getRoot());

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

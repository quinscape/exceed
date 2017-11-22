package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.DataSourceModel;
import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.schema.SchemaService;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class DataSourceUtil
{
    public static void createDataSources(
        ApplicationContext applicationContext,
        Map<String, ? extends DataSourceModel> dataSourceModels,
        Map<String, ExceedDataSource> dataSources,
        String baseName,
        boolean shared
    )
    {

        for (Map.Entry<String, ? extends DataSourceModel> e : dataSourceModels.entrySet())
        {
            final DataSourceModel model = e.getValue();

            model.validate();

            create(
                applicationContext,
                dataSources,
                e.getKey(),
                model,
                baseName,
                shared,
                true
            );
        }

        for (Map.Entry<String, ? extends DataSourceModel> e : dataSourceModels.entrySet())
        {
            create(
                applicationContext,
                dataSources,
                e.getKey(),
                e.getValue(), baseName,
                shared,
                false
            );
        }
    }


    private static void create(
        ApplicationContext applicationContext,
        Map<String, ExceedDataSource> dataSources,
        String dataSourceName,
        DataSourceModel model,
        String baseName,
        boolean shared,
        boolean primary
    )
    {
        final DataSourceResolver resolver = dataSources::get;
        if (model.isShared() == shared && model.isPrimary() == primary)
        {
            final DataSourceFactory factoryBean = applicationContext.getBean(
                model.getDataSourceFactoryName(), DataSourceFactory.class);

            final ExceedDataSource dataSource = factoryBean.create(
                resolver,
                model,
                baseName + "-" + dataSourceName,
                new StorageConfiguration(
                    findBean(NamingStrategy.class, applicationContext, model.getNamingStrategyName()),
                    findBean(DomainOperations.class, applicationContext, model.getDomainOperationsName()),
                    findBean(SchemaService.class, applicationContext, model.getSchemaServiceName())
                )
            );

            dataSources.put(dataSourceName, dataSource);
        }
    }


    private static <T> T findBean(Class<T> cls, ApplicationContext applicationContext, String name)
    {
        if (name == null)
        {
            return null;
        }

        return applicationContext.getBean(name, cls);
    }
}

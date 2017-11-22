package de.quinscape.exceed.runtime.datasrc;


import de.quinscape.exceed.model.staging.JOOQDataSourceModel;
import org.jooq.impl.DataSourceConnectionProvider;

import javax.sql.DataSource;

public class JOOQDataSourceFactory
    extends DelegatingDataSourceFactory<JOOQDataSourceModel>
{
    @Override
    public ExceedDataSource create(
        DataSourceResolver resolver,
        JOOQDataSourceModel model,
        String uniqueName,
        StorageConfiguration storageConfiguration
    )
    {
        final ExceedDataSource targetSource = getTargetDataSource(resolver, model);

        final DataSource targetDataSource = targetSource.getDataSource();

        return new JOOQDataSource(
            model,
            targetDataSource,
            model.getDialect(),
            new DataSourceConnectionProvider(
                targetDataSource
            ),
            targetSource.getStorageConfiguration().merge(storageConfiguration)
        );
    }


    @Override
    public Class<JOOQDataSourceModel> getModelType()
    {
        return JOOQDataSourceModel.class;
    }
}

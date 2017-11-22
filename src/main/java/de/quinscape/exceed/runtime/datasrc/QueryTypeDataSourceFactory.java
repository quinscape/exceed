package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.QueryTypeDataSourceModel;

import javax.sql.DataSource;

public class QueryTypeDataSourceFactory
    extends DelegatingDataSourceFactory<QueryTypeDataSourceModel>
{
    @Override
    public ExceedDataSource create(
        DataSourceResolver resolver, QueryTypeDataSourceModel model, String uniqueName,
        StorageConfiguration storageConfiguration
    )
    {
        final ExceedDataSource targetSource = getTargetDataSource(resolver, model);

        final DataSource targetDataSource = targetSource.getDataSource();

        return new QueryTypeDataSource(
            model,
            targetDataSource,
            targetSource.getStorageConfiguration().merge(storageConfiguration)
        );
    }


    @Override
    public Class<QueryTypeDataSourceModel> getModelType()
    {
        return QueryTypeDataSourceModel.class;
    }
}

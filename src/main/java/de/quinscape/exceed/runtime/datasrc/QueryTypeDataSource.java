package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.DataSourceModel;
import de.quinscape.exceed.model.staging.JOOQDataSourceModel;
import de.quinscape.exceed.model.staging.QueryTypeDataSourceModel;

import javax.sql.DataSource;

public class QueryTypeDataSource
    implements ExceedDataSource
{
    private final QueryTypeDataSourceModel model;

    private final DataSource targetDataSource;

    private final StorageConfiguration storageConfiguration;


    public QueryTypeDataSource(
        QueryTypeDataSourceModel model,
        DataSource targetDataSource,
        StorageConfiguration storageConfiguration
    )
    {
        this.model = model;
        this.targetDataSource = targetDataSource;
        this.storageConfiguration = storageConfiguration;
    }


    @Override
    public DataSourceModel getDataSourceModel()
    {
        return model;
    }


    @Override
    public DataSource getDataSource()
    {
        return targetDataSource;
    }


    @Override
    public StorageConfiguration getStorageConfiguration()
    {
        return storageConfiguration;
    }
}

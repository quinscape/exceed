package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.DataSourceModel;

import javax.sql.DataSource;

public class SystemDataSource
    implements ExceedDataSource
{
    private final String name;

    private final DataSourceModel model;

    private final StorageConfiguration storageConfiguration;


    public SystemDataSource(
        String name, DataSourceModel model, StorageConfiguration storageConfiguration
    )
    {
        this.name = name;
        this.model = model;
        this.storageConfiguration = storageConfiguration;
    }


    public String getName()
    {
        return name;
    }

    @Override
    public DataSourceModel getDataSourceModel()
    {
        return model;
    }


    @Override
    public DataSource getDataSource()
    {
        return null;
    }


    @Override
    public StorageConfiguration getStorageConfiguration()
    {
        return storageConfiguration;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", model = " + model
            + ", storageConfiguration = " + storageConfiguration
            ;
    }
}

package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.AtomikosDataSourceModel;

import javax.sql.DataSource;

/**
 * An Exceed JOOQ data source encapsulating an SQL data source and a JOOQ dslContext connected to it.
 */
public class AtomikosDataSource
    implements ExceedDataSource
{
    private final DataSource dataSource;

    private final StorageConfiguration storageConfiguration;

    private final AtomikosDataSourceModel dataSourceModel;


    public AtomikosDataSource(
        AtomikosDataSourceModel dataSourceModel, DataSource dataSource,
        StorageConfiguration storageConfiguration
    )
    {
        this.dataSourceModel = dataSourceModel;
        this.dataSource = dataSource;
        this.storageConfiguration = storageConfiguration;
    }


    @Override
    public AtomikosDataSourceModel getDataSourceModel()
    {
        return dataSourceModel;
    }


    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }


    public StorageConfiguration getStorageConfiguration()
    {
        return storageConfiguration;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "dataSource = " + dataSource
            + ", storageConfiguration = " + storageConfiguration
            + ", dataSourceModel = " + dataSourceModel
            ;
    }
}



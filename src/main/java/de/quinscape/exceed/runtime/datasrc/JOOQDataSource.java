package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.JOOQDataSourceModel;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;

import javax.sql.DataSource;

/**
 * An Exceed JOOQ data source encapsulating an SQL data source and a JOOQ dslContext connected to it.
 */
public class JOOQDataSource
    implements ExceedDataSource
{
    private final JOOQDataSourceModel dataSourceModel;

    private final DataSource dataSource;

    private final DSLContext dslContext;

    private final StorageConfiguration storageConfiguration;


    public JOOQDataSource(
        JOOQDataSourceModel dataSourceModel, DataSource dataSource,
        SQLDialect dialect, ConnectionProvider connectionProvider,
        StorageConfiguration storageConfiguration
    )
    {
        this.dataSourceModel = dataSourceModel;
        final Configuration config = new DefaultConfiguration().derive(connectionProvider).derive(dialect);
        this.dataSource = dataSource;
        this.dslContext = new DefaultDSLContext(config);
        this.storageConfiguration = storageConfiguration;
    }


    public DSLContext getDslContext()
    {
        return dslContext;
    }


    @Override
    public JOOQDataSourceModel getDataSourceModel()
    {
        return dataSourceModel;
    }


    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }


    @Override
    public StorageConfiguration getStorageConfiguration()
    {
        return storageConfiguration;
    }
}



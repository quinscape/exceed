package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.DataSourceModel;

import javax.sql.DataSource;

/**
 * Implemented by exceed data source implementations.
 */
public interface ExceedDataSource
{
    DataSourceModel getDataSourceModel();

    DataSource getDataSource();

    StorageConfiguration getStorageConfiguration();
}

package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.DataSourceModel;

public interface DelegatingDataSourceModel
    extends DataSourceModel
{
    String getDataSource();
}

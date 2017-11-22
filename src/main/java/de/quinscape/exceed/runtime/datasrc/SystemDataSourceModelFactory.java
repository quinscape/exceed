package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.SystemDataSourceModel;

public class SystemDataSourceModelFactory
    implements DataSourceFactory<SystemDataSourceModel>
{
    @Override
    public ExceedDataSource create(
        DataSourceResolver resolver,
        SystemDataSourceModel model,
        String uniqueName,
        StorageConfiguration storageConfiguration
    )
    {
        return new SystemDataSource(uniqueName, model, storageConfiguration);
    }

    @Override
    public Class<SystemDataSourceModel> getModelType()
    {
        return SystemDataSourceModel.class;
    }
}

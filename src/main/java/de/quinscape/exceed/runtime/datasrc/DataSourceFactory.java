package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.model.staging.AbstractDataSourceModel;
import de.quinscape.exceed.model.staging.DataSourceModel;

/**
 * Creates a data source from a data source model. Each implementation of {@link AbstractDataSourceModel} must have a corresponding
 * implementation of this factory.
 *
 * @param <M> Data source model type
 */
public interface DataSourceFactory<M extends DataSourceModel>
{
    ExceedDataSource create(
        DataSourceResolver resolver,
        M model,
        String uniqueName,
        StorageConfiguration storageConfiguration
    );

    Class<M> getModelType();


}

package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.runtime.model.InconsistentModelException;

public abstract class DelegatingDataSourceFactory<T extends DelegatingDataSourceModel>
    implements DataSourceFactory<T>
{
    protected ExceedDataSource getTargetDataSource(DataSourceResolver resolver, T model)
    {
        final String dataSource = model.getDataSource();

        final ExceedDataSource targetSource = resolver.resolve(dataSource);


        if (targetSource == null)
        {
            throw new InconsistentModelException("Target data source '" + dataSource + "' does not exist.");

        }
        else if (targetSource.getDataSource() == null)
        {
            throw new InconsistentModelException("Target data source '" + dataSource + "' has no SQL data source");
        }
        return targetSource;
    }
}

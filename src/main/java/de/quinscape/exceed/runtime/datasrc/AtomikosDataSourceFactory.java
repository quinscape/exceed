package de.quinscape.exceed.runtime.datasrc;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import de.quinscape.exceed.model.staging.AtomikosDataSourceModel;

/**
 * Factory for atomikos managed data sources in exceed. Creates an {@link AtomikosDataSource} from an
 * {@link AtomikosDataSourceModel}.
 */
public class AtomikosDataSourceFactory
    implements DataSourceFactory<AtomikosDataSourceModel>
{
    @Override
    public ExceedDataSource create(
        DataSourceResolver resolver,
        AtomikosDataSourceModel model,
        String uniqueName,
        StorageConfiguration storageConfiguration
    )
    {
        final AtomikosDataSourceBean dataSourceBean = model.getAtomikosDataSourceBean();

        dataSourceBean.setUniqueResourceName(uniqueName);


        return new AtomikosDataSource(model, dataSourceBean, storageConfiguration);
    }


    @Override
    public Class<AtomikosDataSourceModel> getModelType()
    {
        return AtomikosDataSourceModel.class;
    }
}

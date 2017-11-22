package de.quinscape.exceed.runtime.action.param;

import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ParameterProvider;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.datasrc.JOOQDataSource;

public class DataSourceProvider
    implements ParameterProvider
{
    private final String dataSourceName;

    private final boolean injectDSLContext;


    public DataSourceProvider(String dataSourceName, boolean injectDSLContext)
    {
        this.dataSourceName = dataSourceName;
        this.injectDSLContext = injectDSLContext;
    }


    @Override
    public Object provide(RuntimeContext runtimeContext)
    {

        final ExceedDataSource dataSource = runtimeContext.getDomainService().getDataSource(dataSourceName);

        if (injectDSLContext)
        {
            if (!(dataSource instanceof JOOQDataSource))
            {
                final ApplicationConfig configModel = runtimeContext.getApplicationModel().getConfigModel();
                final String effectiveName = dataSourceName != null ? dataSourceName : configModel.getDefaultDataSource();
                throw new IllegalStateException("Data source '" + effectiveName + "' is not an JOOQ data source");
            }

            return ((JOOQDataSource) dataSource).getDslContext();
        }
        else
        {
            return dataSource;
        }
    }
}

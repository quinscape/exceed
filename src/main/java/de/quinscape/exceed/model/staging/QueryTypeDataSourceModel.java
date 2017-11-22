package de.quinscape.exceed.model.staging;

import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.datasrc.DelegatingDataSourceModel;
import de.quinscape.exceed.runtime.model.InconsistentModelException;

/**
 * Data source model used for query types.  
 */
public class QueryTypeDataSourceModel
    extends AbstractDataSourceModel
    implements DelegatingDataSourceModel
{
    private static final String QUERY_TYPE_SOURCE_FACTORY = "queryTypeDataSourceFactory";

    private String dataSource;


    /**
     * Name of the target data source, an actual data base data source. (currently of type "xcd.staging.AtomikosDataSourceModel")
     */
    @Override
    public String getDataSource()
    {
        return dataSource;
    }


    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
    }


    @Override
    @Internal
    public boolean isPrimary()
    {
        return false;
    }


    @Override
    @Internal
    public String getDataSourceFactoryName()
    {
        return QUERY_TYPE_SOURCE_FACTORY;
    }

    @Override
    public void validate()
    {
        if (dataSource == null)
        {
            throw new InconsistentModelException("JOOQDataSourceModel has no target dataSource reference. You must define" +
                "a property \"dataSource\" naming the actual data source model.");
        }
    }
}

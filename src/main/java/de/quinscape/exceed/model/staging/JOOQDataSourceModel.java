package de.quinscape.exceed.model.staging;

import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.datasrc.DelegatingDataSourceModel;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.jooq.SQLDialect;

/**
 * A JOOQ-based SQL data source. It encapsulates additional JOOQ related objects for a referenced target data source.
 */
public class JOOQDataSourceModel
    extends AbstractDataSourceModel
    implements DelegatingDataSourceModel
{
    private static final String JOOQ_DATA_SOURCE_FACTORY = "jooqDataSourceFactory";

    private SQLDialect dialect;

    private String dataSource;

    /**
     * JOOQ sql dialect
     */
    public SQLDialect getDialect()
    {
        return dialect;
    }


    public void setDialect(SQLDialect dialect)
    {
        this.dialect = dialect;
    }


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
        return JOOQ_DATA_SOURCE_FACTORY;
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

package de.quinscape.exceed.model.staging;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import de.quinscape.exceed.model.annotation.DocumentedCollection;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONProperty;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Wraps a AtomikosDataSourceBean, either ignoring or converting properties that are inconveniently typed for our model word.
 *
 * <ul>
 *     <li>Properties-typed properties are converted from Map<String,String></li>
 *     <li>Non-JSONable properties are just ignored. If you need those, implement an alternative data source factory implementation</li>
 * </ul>
 * <p>
 *     The property description have been taken from the atomikos data source bean documentation
 * </p>
 */
public class AtomikosDataSourceModel
    extends AbstractDataSourceModel
{
    private static final String ATOMIKOS_DATA_SOURCE_FACTORY = "atomikosDataSourceFactory";

    private final AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();

    private String dataSourceFactoryName = ATOMIKOS_DATA_SOURCE_FACTORY;


    public void setDataSourceFactoryName(String dataSourceFactoryName)
    {
        this.dataSourceFactoryName = dataSourceFactoryName;
    }


    @Override
    public boolean isPrimary()
    {
        return true;
    }


    /**
     * Spring bean name of the data source factory for this atomikos data source model.
     *
     * Defaults to "atomikosDataSourceFactory".
     * @return
     */
    @Override
    public String getDataSourceFactoryName()
    {
        return dataSourceFactoryName;
    }


    @JSONProperty(ignore = true)
    public AtomikosDataSourceBean getAtomikosDataSourceBean()
    {
        return atomikosDataSourceBean;
    }


    /**
     * Sets the minimum pool size. The amount of pooled connections won't go
     * below that value. The pool will open this amount of connections during
     * initialization. Optional, defaults to 1.
     */
    public int getMinPoolSize()
    {
        return atomikosDataSourceBean.getMinPoolSize();
    }


    public void setMinPoolSize(int minPoolSize)
    {
        atomikosDataSourceBean.setMinPoolSize(minPoolSize);
    }


    public int getMaxPoolSize()
    {
        return atomikosDataSourceBean.getMaxPoolSize();
    }


    /**
     * The maximum pool size. The amount of pooled connections won't go
     * above this value. Optional, defaults to 1.
     *
     */
    public void setMaxPoolSize(int maxPoolSize)
    {
        atomikosDataSourceBean.setMaxPoolSize(maxPoolSize);
    }


    /**
     * The maximum amount of time in seconds the pool will block
     * waiting for a connection to become available in the pool when it
     * is empty. Optional.
     *
     * Defaults to 30 seconds.
     */
    public int getBorrowConnectionTimeout()
    {
        return atomikosDataSourceBean.getBorrowConnectionTimeout();
    }


    public void setBorrowConnectionTimeout(int borrowConnectionTimeout)
    {
        atomikosDataSourceBean.setBorrowConnectionTimeout(borrowConnectionTimeout);
    }


    /**
     * Sets the amount of time (in seconds) that the connection pool will allow a connection
     * to be in use, before claiming it back. Optional.
     *
     * Default is 0 (no timeout).
     */
    public int getReapTimeout()
    {
        return atomikosDataSourceBean.getReapTimeout();
    }


    public void setReapTimeout(int reapTimeout)
    {
        atomikosDataSourceBean.setReapTimeout(reapTimeout);
    }


    public void setMaintenanceInterval(int maintenanceInterval)
    {
        atomikosDataSourceBean.setMaintenanceInterval(maintenanceInterval);
    }


    /**
     * The maintenance interval for the pool maintenance thread. Optional.
     */
    public int getMaintenanceInterval()
    {
        return atomikosDataSourceBean.getMaintenanceInterval();
    }


    /**
     * The maximum amount of seconds that unused excess connections should stay in the pool. Optional.
     *
     * The default is 60 seconds.
     */
    public int getMaxIdleTime()
    {
        return atomikosDataSourceBean.getMaxIdleTime();
    }


    public void setMaxIdleTime(int maxIdleTime)
    {
        atomikosDataSourceBean.setMaxIdleTime(maxIdleTime);
    }


    public void setMaxLifetime(int maxLifetime)
    {
        atomikosDataSourceBean.setMaxLifetime(maxLifetime);
    }


    /**
     * The maximum amount of seconds that a connection is kept in the pool before
     * it is destroyed automatically. Optional, defaults to 0 (no limit).
     */
    public int getMaxLifetime()
    {
        return atomikosDataSourceBean.getMaxLifetime();
    }


    /**
     * The SQL query or statement used to validate a connection before returning it. Optional.
     *
     * The SQL query or statement to validate the connection with. Note that
     * although you can specify updates here, these will NOT be part of any JTA transaction!
     */
    public String getTestQuery()
    {
        return atomikosDataSourceBean.getTestQuery();
    }


    public void setTestQuery(String testQuery)
    {
        atomikosDataSourceBean.setTestQuery(testQuery);
    }


    public int getLoginTimeout() throws SQLException
    {
        return atomikosDataSourceBean.getLoginTimeout();
    }


    public void setLoginTimeout(int seconds) throws SQLException
    {
        atomikosDataSourceBean.setLoginTimeout(seconds);
    }

    /**
     * The default isolation level of connections returned by this datasource.
     * Optional, defaults to the vendor-specific JDBC or DBMS settings.
     */
    public boolean getLocalTransactionMode()
    {
        return atomikosDataSourceBean.getLocalTransactionMode();
    }


    public void setDefaultIsolationLevel(int defaultIsolationLevel)
    {
        atomikosDataSourceBean.setDefaultIsolationLevel(defaultIsolationLevel);
    }


    /**
     * Gets the default isolation level for connections created by this datasource.
     */
    public int getDefaultIsolationLevel()
    {
        return atomikosDataSourceBean.getDefaultIsolationLevel();
    }


    /**
     * Sets the properties (name,value pairs) used to
     * configure the XADataSource. Required.
     */
    @DocumentedCollection(
        keyDesc = "name",
        valueDesc= "value"
    )
    public Map<String,String> getXaProperties()
    {
        return Util.propertiesToMap(atomikosDataSourceBean.getXaProperties());
    }


    public void setXaProperties(Map<String,String> xaProperties)
    {
        atomikosDataSourceBean.setXaProperties(Util.mapToProperties(xaProperties));
    }


    /**
     * The fully qualified underlying XADataSource class name. Required.
     */
    public String getXaDataSourceClassName()
    {
        return atomikosDataSourceBean.getXaDataSourceClassName();
    }


    public void setXaDataSourceClassName(String xaDataSourceClassName)
    {
        atomikosDataSourceBean.setXaDataSourceClassName(xaDataSourceClassName);
    }
}

package de.quinscape.exceed.runtime.db;

import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Required;

public class JOOQConfigFactory
{
    private ConnectionProvider connectionProvider;
    
    private SQLDialect dialect;
    
    @Required
    public void setDialect(SQLDialect dialect)
    {
        this.dialect = dialect;
    }
    
    @Required
    public void setConnectionProvider(ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
    }
    
    public Configuration create()
    {
        return new DefaultConfiguration().derive(connectionProvider).derive(dialect);
    }
    
}

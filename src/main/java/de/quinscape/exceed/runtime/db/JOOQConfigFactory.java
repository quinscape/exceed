package de.quinscape.exceed.runtime.db;

import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

public class JOOQConfigFactory
{
    private final ConnectionProvider connectionProvider;
    
    private final SQLDialect dialect;


    public JOOQConfigFactory(ConnectionProvider connectionProvider, SQLDialect dialect)
    {
        this.connectionProvider = connectionProvider;
        this.dialect = dialect;
    }


    public Configuration create()
    {
        return new DefaultConfiguration().derive(connectionProvider).derive(dialect);
    }
    
}

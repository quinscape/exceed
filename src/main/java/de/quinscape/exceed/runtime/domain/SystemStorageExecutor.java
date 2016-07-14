package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemStorageExecutor
    implements QueryExecutor
{
    private final static Logger log = LoggerFactory.getLogger(SystemStorageExecutor.class);


    @Override
    public Object execute(RuntimeContext runtimeContext, QueryDefinition queryDefinition)
    {
        log.info("MODEL QUERY: {}", queryDefinition);

        return "RESULT";
    }
}

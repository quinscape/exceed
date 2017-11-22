package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;

import java.util.List;

public class NoopSchemaService
    implements SchemaService
{
    @Override
    public void synchronizeSchema(
        RuntimeContext runtimeContext, ExceedDataSource dataSource, List<DomainType> value
    )
    {
        
    }


    @Override
    public void removeSchema(RuntimeContext runtimeContext, ExceedDataSource dataSource)
    {

    }
}

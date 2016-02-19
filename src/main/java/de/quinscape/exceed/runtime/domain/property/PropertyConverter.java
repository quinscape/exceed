package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

public interface PropertyConverter<P, D>
{
    P convertToJava(RuntimeContext runtimeContext, D value, DomainProperty property) throws ParseException;
    D convertToJSON(RuntimeContext runtimeContext, P value, DomainProperty property);

    Class<P> getJavaType();
    Class<D> getJSONType();
}

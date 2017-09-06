package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PropertyConverter<P, D, J>
{
    Logger log = LoggerFactory.getLogger(PropertyConverter.class);

    P convertToJava(RuntimeContext runtimeContext, D value);
    D convertToJSON(RuntimeContext runtimeContext, P value);

    J convertToJs(RuntimeContext runtimeContext, P value);
    P convertFromJs(RuntimeContext runtimeContext, J value);

    Class<P> getJavaType();
    Class<D> getJSONType();
}

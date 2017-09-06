package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.util.JsUtil;
import jdk.nashorn.api.scripting.AbstractJSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DomainServiceCreateFunction
    extends AbstractJSObject
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceCreateFunction.class);


    private final DomainService domainService;

    public DomainServiceCreateFunction(DomainService domainService)
    {
        this.domainService = domainService;
    }


    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        final String type = (String) args[0];
        final String id = args.length > 1 ? JsUtil.get(args[1]) : null;
        log.debug("Create wrapped domain object for type '{}'", type);

        final RuntimeContext runtimeContext = RuntimeContextHolder.get();

        final JsEnvironment env = runtimeContext.getJsEnvironment();
        final DomainObject domainObject = domainService.create(runtimeContext, type, id != null ? id : UUID.randomUUID().toString());


        final PropertyType propertyType = PropertyType.get(
            runtimeContext,
            DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, type)
                .build()
        );

        return propertyType.convertToJs(
            runtimeContext,
            domainObject
        );
    }
}

package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainUtil
{
    private final static Logger log = LoggerFactory.getLogger(DomainUtil.class);


    public static DomainObject copy(RuntimeContext runtimeContext, DomainObject domainObject)
    {
        if (domainObject == null)
        {
            return null;
        }


        DomainObject copy = domainObject.getDomainService().create(domainObject.getDomainType(), domainObject.getId());
        DomainUtil.copyProperties(runtimeContext, domainObject, copy, false);
        return copy;
    }


    /**
     * Copies the declared properties from one give domain object to another
     *
     * @param runtimeContext        runtime context
     * @param from                  source domain object
     * @param to                    target domain object
     * @param convertToJSON         <code>true</code> if the properties should be converted to their JSON format, otherwise
     *                              they're copied as-is.
     */
    public static void copyProperties(RuntimeContext runtimeContext, DomainObject from, DomainObject to, boolean
        convertToJSON)
    {
        DomainService domainService = from.getDomainService();
        DomainType domainType = domainService.getDomainType(from.getDomainType());

        for (DomainProperty property : domainType.getProperties())
        {
            String name = property.getName();
            Object value = from.getProperty(name);

            if (convertToJSON)
            {
                PropertyConverter propertyConverter = domainService.getPropertyConverter(property.getType());
                value = propertyConverter.convertToJSON(runtimeContext, value);
            }
            to.setProperty(name, value);
        }
    }

    public static DomainObject convertToJava(RuntimeContext runtimeContext, DomainObject domainObject) throws
        ParseException

    {
        if (domainObject == null)
        {
            return null;
        }

        final DomainService domainService = domainObject.getDomainService();

        DomainType domainType = domainService.getDomainType(domainObject.getDomainType());
        for (DomainProperty property : domainType.getProperties())
        {
            String propertyName = property.getName();
            Object value = domainObject.getProperty(propertyName);
            PropertyConverter propertyConverter = domainService.getPropertyConverter(property.getType());

            if (propertyConverter == null)
            {
                throw new IllegalStateException("No converter for property '" + property.getType() + "'");
            }

            Object converted = propertyConverter.convertToJava(runtimeContext, value);

            domainObject.setProperty(propertyName, converted);
        }

        return domainObject;
    }


    public static DomainObject convertToJSON(RuntimeContext runtimeContext, DomainObject domainObject)
    {
        final DomainService domainService = domainObject.getDomainService();

        DomainType domainType = domainService.getDomainType(domainObject.getDomainType());

        log.debug("Converting domain object of type '{}'", domainType.getName());

        DomainObject copy = domainObject.getDomainService().create(domainObject.getDomainType(), domainObject.getId());

        for (DomainProperty property : domainType.getProperties())
        {
            String propertyName = property.getName();
            Object value = domainObject.getProperty(propertyName);
            PropertyConverter propertyConverter = domainService.getPropertyConverter(property.getType());

            if (propertyConverter == null)
            {
                throw new IllegalStateException("No converter for property '" + property.getType() + "'");
            }

            log.debug("Converting property '{}': in = {}", propertyName, value);

            Object converted = propertyConverter.convertToJSON(runtimeContext, value);

            copy.setProperty(propertyName, converted);
        }

        return copy;
    }
}

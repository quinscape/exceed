package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.JSONUtil;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class AbstractConvertingFunction
    extends AbstractJSObject
{
    private final static Logger log = LoggerFactory.getLogger(AbstractConvertingFunction.class);


    protected final ScriptContext scriptContext;

    protected final ApplicationModel applicationModel;

    protected final NashornScriptEngine nashorn;

    protected final Object UNDEFINED = new Object();

    public AbstractConvertingFunction(
        NashornScriptEngine nashorn,
        ScriptContext scriptContext,
        ApplicationModel applicationModel)
    {
        this.nashorn = nashorn;
        this.scriptContext = scriptContext;
        this.applicationModel = applicationModel;


    }




    protected void setPath(RuntimeContext runtimeContext, Object path, Object value) throws ParseException
    {
        getOrSetPath(runtimeContext, path, value);
    }

    protected Object getPath(RuntimeContext runtimeContext, Object path) throws ParseException
    {
        return getOrSetPath(runtimeContext, path, UNDEFINED);
    }

    private Object getOrSetPath(RuntimeContext runtimeContext, Object path, Object value) throws ParseException
    {
        final JsEnvironment jsEnvironment = runtimeContext.getJsEnvironment();
        final String name;
        final ScriptObjectMirror mirror;
        if (path instanceof String)
        {
            name = (String) path;
            mirror = null;
        }
        else  if (path instanceof ScriptObjectMirror)
        {
            mirror = (ScriptObjectMirror) path;
            name = (String) mirror.getSlot(0);
        }
        else
        {
            throw new IllegalStateException("Invalid scope path: " + path);
        }

        final boolean isSetOperation = value != UNDEFINED;


        PropertyModel currentType = runtimeContext.getScopedContextChain().getModel(name);
        Object currentValue = runtimeContext.getScopedContextChain().getProperty(name);

        final int len = mirror == null ? 1 : (int) mirror.get("length");
        final int last = len - 1;
        for (int i = 1 ; i < len; i++)
        {
            Object nextProp = mirror.getSlot(i);

            final boolean isLastInSetOperation = i == last && isSetOperation;

            if (nextProp instanceof Number)
            {
                final int index = ((Number) nextProp).intValue();

                if (!currentType.getType().equals(PropertyType.LIST))
                {
                    throw new IllegalStateException("Invalid index access to property: " + ExpressionUtil.describe(currentType));
                }

                currentType = ExpressionUtil.getCollectionType(applicationModel, currentType.getTypeParam());

                if (currentValue instanceof Collection)
                {
                    if (currentValue instanceof List)
                    {
                        if (!isLastInSetOperation)
                        {
                            currentValue = ((List) currentValue).get(index);
                        }
                        else
                        {
                            final PropertyType propertyType = PropertyType.get(
                                runtimeContext,
                                currentType
                            );

                            final Object converted = propertyType.convertFromJs(runtimeContext, value);
                            ((List) currentValue).set(index, converted);
                        }
                    }
                    else
                    {
                        if (value == UNDEFINED)
                        {
                            final Iterator iterator = ((Collection) currentValue).iterator();
                            for (int j = index; j > 0; j--)
                            {
                                currentValue = iterator.next();
                            }
                        }
                        else
                        {
                            throw new IllegalStateException("Cannot set index of non-list " + currentValue);
                        }
                    }
                }
                else
                {
                    throw new IllegalStateException("Invalid index access to property: " + ExpressionUtil.describe(currentType));
                }
            }
            else if (nextProp instanceof String)
            {
                if (currentType.getType().equals(PropertyType.MAP))
                {
                    currentType = ExpressionUtil.getCollectionType(applicationModel, currentType.getTypeParam());
                    if (!(currentValue instanceof Map))
                    {
                        throw new IllegalStateException("Invalid map property value: " + currentValue);
                    }

                    if (!isLastInSetOperation)
                    {
                        currentValue = ((Map) currentValue).get(nextProp);
                    }
                    else
                    {
                        final PropertyType propertyType = PropertyType.get(
                            runtimeContext,
                            currentType
                        );
                        final Object converted = propertyType.convertFromJs(runtimeContext, value);
                        ((Map) currentValue).put(nextProp, converted);
                    }

                }
                else  if (currentType.getType().equals(PropertyType.DOMAIN_TYPE))
                {
                    final String domainTypeName = currentType.getTypeParam();
                    final DomainType domainType = applicationModel.getDomainType(getType(domainTypeName, currentValue));
                    if (domainType == null)
                    {
                        throw new IllegalStateException("Domain type '" + domainTypeName + "' not found");
                    }

                    currentType = domainType.getProperty((String) nextProp);


                    if (isLastInSetOperation)
                    {
                        final PropertyType propertyType = PropertyType.get(
                            runtimeContext,
                            currentType
                        );
                        final Object converted = propertyType.convertFromJs(runtimeContext, value);
                        JSONUtil.DEFAULT_UTIL.setProperty(currentValue, (String) nextProp, converted);
                    }
                    else
                    {
                        currentValue = JSONUtil.DEFAULT_UTIL.getProperty(currentValue, (String) nextProp);
                    }
                }
                else
                {
                    throw new IllegalStateException("Cannot access property '" + nextProp + "' of type " + ExpressionUtil.describe(currentType));
                }
            }
            else
            {
                throw new IllegalStateException("Invalid property name value: " + nextProp);
            }
        }

        if (isSetOperation)
        {
            return null;
        }
        else
        {
            final PropertyType propertyType = PropertyType.get(
                runtimeContext,
                currentType
            );
            final Object converted = propertyType.convertToJs(runtimeContext, currentValue);

            log.debug("Context value '{}' is {} (type = {})", name, converted, currentType);

            return converted;
        }
    }


    private String getType(String domainTypeName, Object domainObjectBean)
    {
        if (domainTypeName != null)
        {
            return domainTypeName;
        }
        return (String) JSONUtil.DEFAULT_UTIL.getProperty(domainObjectBean, "_type");
    }
}

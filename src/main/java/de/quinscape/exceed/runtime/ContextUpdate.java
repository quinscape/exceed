package de.quinscape.exceed.runtime;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ContextUpdate
{
    private final static Logger log = LoggerFactory.getLogger(ContextUpdate.class);

    public static void convertToJava(
        RuntimeContext runtimeContext, Process process, View view, Map<String, Object> contextUpdate
    ) throws ParseException
    {
        final ContextModel applicationContext = runtimeContext.getApplicationModel().getConfigModel().getApplicationContextModel();
        final ContextModel sessionContext = runtimeContext.getApplicationModel().getConfigModel().getSessionContextModel();
        final ContextModel viewContextModel = view.getContextModel();

        convertContextUpdateToJava(runtimeContext, contextUpdate, applicationContext);
        convertContextUpdateToJava(runtimeContext, contextUpdate, sessionContext);
        if (process != null)
        {
            final ContextModel processContext = process.getContextModel();
            convertContextUpdateToJava(runtimeContext, contextUpdate, processContext);
        }
        convertContextUpdateToJava(runtimeContext, contextUpdate, viewContextModel);
    }

    private static void convertContextUpdateToJava(
        RuntimeContext runtimeContext, Map<String, Object> contextUpdate, ContextModel contextModel
    ) throws ParseException
    {
        if (contextModel != null)
        {
            for (Map.Entry<String, ScopedPropertyModel> e : contextModel.getProperties().entrySet())
            {
                final String name = e.getKey();
                final ScopedPropertyModel model = e.getValue();

                if (contextUpdate.containsKey(name))
                {
                    final Object value = contextUpdate.get(name);

                    final PropertyConverter propertyConverter = model.getPropertyType();
                    final Object converted = propertyConverter.convertToJava(runtimeContext, value);

                    if (log.isDebugEnabled())
                    {
                        log.debug("Convert property '{}': {} => {}", name, value, converted);
                    }

                    if (model.getType().equals(PropertyType.DOMAIN_TYPE))
                    {
                        final DomainObject domainObject = (DomainObject) converted;
                        if (domainObject.getId() != null)
                        {
                            DomainUtil.merge(runtimeContext, domainObject);
                        }
                    }

                    contextUpdate.put(name, converted);
                }
            }
        }
    }

}

package de.quinscape.exceed.runtime.process;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedObjectModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Contains context data for a transition invocation.
 *
 * When one of the transitions defined in the current process view state is triggered from the outside, there domain object data
 * to transmit along that transition this data contains:
 *
 *  <ul>
 *      <li>The domain object context for the transition execution which is extracted from the data cursor of TButton</li>
 *      <li>Updates for context values the process allowed the user to edit via input fields or similar</li>
 *  </ul>
 *
 *  @see #parse(RuntimeContext, String, String, String)
 */
public class TransitionData
{
    private final static Logger log = LoggerFactory.getLogger(TransitionData.class);

    private GenericDomainObject objectContext;

    private Map<String, Object> contextUpdate;

    private String stateName;


    public GenericDomainObject getObjectContext()
    {
        return objectContext;
    }


    public String getStateName()
    {
        return stateName;
    }


    public void setStateName(String stateName)
    {
        this.stateName = stateName;
    }


    public void setObjectContext(GenericDomainObject objectContext)
    {

        this.objectContext = objectContext;
    }


    public Map<String, Object> getContextUpdate()
    {
        if (contextUpdate == null)
        {
            return Collections.emptyMap();
        }
        return contextUpdate;
    }


    public void setContextUpdate(Map<String, Object> contextUpdate)
    {
        this.contextUpdate = contextUpdate;
    }


    /**
     * Parses the given transition data JSON and converts it according to the context model definitions involved.
     *
     * @param runtimeContext        runtime context
     * @param processName           process name
     * @param currentState          current view state
     * @param json                  JSON data
     * @return converted transition data instance
     *
     * @throws ParseException if conversion fails
     */
    public static TransitionData parse(RuntimeContext runtimeContext, String processName, String currentState, String
        json) throws ParseException


    {
        GenericDomainObject partialDomainObjectContext;
        log.debug("Domain Object context: {}", json);

        TransitionData transitionData = runtimeContext.getDomainService().toDomainObject(TransitionData.class, json);
        transitionData.setStateName(currentState);
        partialDomainObjectContext = transitionData.getObjectContext();

        final Process process = runtimeContext.getApplicationModel().getProcess(processName);

        if (process == null)
        {
            throw new IllegalStateException("Process '" + processName + "' not found.");
        }

        convertToJava(runtimeContext, partialDomainObjectContext, process, currentState, transitionData);

        return transitionData;
    }


    private static void convertToJava(RuntimeContext runtimeContext, GenericDomainObject partialDomainObjectContext,
                                      Process
                                          process, String currentState, TransitionData transitionData) throws
        ParseException
    {
        final View view = process.getView(currentState);
        if (view == null)
        {
            throw new IllegalStateException("View '" + currentState + "' not found in process '" + process.getName()
                + "'");
        }

        convertDomainObjectContext(runtimeContext, partialDomainObjectContext, transitionData);

        convertContextToJava(runtimeContext, transitionData, runtimeContext.getApplicationModel()
            .getApplicationContext());
        convertContextToJava(runtimeContext, transitionData, runtimeContext.getApplicationModel().getSessionContext());
        convertContextToJava(runtimeContext, transitionData, process.getContext());
        convertContextToJava(runtimeContext, transitionData, view.getContextModel());

    }


    private static void convertDomainObjectContext(RuntimeContext runtimeContext, GenericDomainObject
        partialDomainObjectContext, TransitionData transitionData) throws ParseException
    {
        partialDomainObjectContext = (GenericDomainObject) DomainUtil.convertToJava(runtimeContext,
            partialDomainObjectContext);
        transitionData.setObjectContext(partialDomainObjectContext);
    }


    private static void convertContextToJava(RuntimeContext runtimeContext, TransitionData transitionData, ContextModel
        contextModel) throws ParseException
    {
        // XXX: convert lists?
        convertObjects(runtimeContext, transitionData, contextModel);
        convertProperties(runtimeContext, transitionData, contextModel);
    }


    private static void convertProperties(RuntimeContext runtimeContext, TransitionData transitionData, ContextModel
        contextModel) throws ParseException
    {
        final DomainService domainService = runtimeContext.getDomainService();
        final Map<String, Object> contextUpdate = transitionData.getContextUpdate();
        for (Map.Entry<String, ScopedPropertyModel> e : contextModel.getProperties().entrySet())
        {
            final String name = e.getKey();
            final ScopedPropertyModel model = e.getValue();

            if (contextUpdate.containsKey(name))
            {
                final Object value = contextUpdate.get(name);
                log.debug("Convert property '{}': {}", name, value);

                final PropertyConverter propertyConverter = domainService.getPropertyConverter(model.getType());
                final Object converted = propertyConverter.convertToJava(runtimeContext, value);

                contextUpdate.put(name, converted);
            }
        }
    }

    private static void convertObjects(RuntimeContext runtimeContext, TransitionData transitionData, ContextModel
        contextModel) throws ParseException
    {
        final DomainService domainService = runtimeContext.getDomainService();
        final Map<String, Object> contextUpdate = transitionData.getContextUpdate();

        for (Map.Entry<String, ScopedObjectModel> e : contextModel.getObjects().entrySet())
        {
            final String name = e.getKey();
            final ScopedObjectModel model = e.getValue();

            if (contextUpdate.containsKey(name))
            {
                final Map<String, Object> m = (Map<String, Object>) contextUpdate.get(name);

                final DomainType domainType = domainService.getDomainType(model.getType());
                if (domainType == null)
                {
                    throw new IllegalStateException("Can't find domain type '" + model.getType() + "'");
                }

                final DomainObject domainObject;

                if (m == null)
                {
                    domainObject = null;
                }
                else
                {
                    domainObject = domainService.create(model.getType(), (String) m.get(DomainType.ID_PROPERTY));

                    log.debug("Convert domain object '{}': {}", name, m);

                    for (DomainProperty property : domainType.getProperties())
                    {

                        final Object value = m.get(property.getName());
                        final PropertyConverter propertyConverter = domainService.getPropertyConverter(property.getType());
                        final Object converted = propertyConverter.convertToJava(runtimeContext, value);

                        domainObject.setProperty(property.getName(), converted);
                    }
                }

                contextUpdate.put(name, domainObject);
            }
        }
    }
}

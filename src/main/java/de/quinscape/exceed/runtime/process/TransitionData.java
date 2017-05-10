package de.quinscape.exceed.runtime.process;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

import java.util.Collections;
import java.util.Map;

/**
 * Contains context data for a transition invocation.
 * <p>
 * When one of the transitions defined in the current process view state is triggered from the outside, there domain
 * object data
 * to transmit along that transition this data contains:
 * <p>
 * <ul>
 * <li>The domain object context for the transition execution which is extracted from the data cursor of TButton</li>
 * <li>Updates for context values the process allowed the user to edit via input fields or similar</li>
 * </ul>
 *
 * @see #parse(RuntimeContext, String, String, String)
 */
public class TransitionData
{
    private final static Logger log = LoggerFactory.getLogger(TransitionData.class);

    private GenericDomainObject objectContext;

    private Map<String, Object> contextUpdate;

    private Map<String, String> currentViewChanges;

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
     * @param runtimeContext runtime context
     * @param processName    process name
     * @param currentState   current view state
     * @param json           JSON data
     * @return converted transition data instance
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

        final Map<String, Object> contextUpdate = transitionData.getContextUpdate();

        final ContextModel applicationContext = runtimeContext.getApplicationModel().getApplicationContextModel();
        final ContextModel sessionContext = runtimeContext.getApplicationModel().getSessionContextModel();
        final ContextModel viewContextModel = view.getContextModel();
        final ContextModel processContext = process.getContextModel();

        convertContextUpdateToJava(runtimeContext, contextUpdate, applicationContext);
        convertContextUpdateToJava(runtimeContext, contextUpdate, sessionContext);
        convertContextUpdateToJava(runtimeContext, contextUpdate, processContext);
        convertContextUpdateToJava(runtimeContext, contextUpdate, viewContextModel);
    }


    private static void convertDomainObjectContext(RuntimeContext runtimeContext, GenericDomainObject
        partialDomainObjectContext, TransitionData transitionData) throws ParseException
    {
        partialDomainObjectContext = (GenericDomainObject) DomainUtil.convertToJava(runtimeContext,
            partialDomainObjectContext);
        transitionData.setObjectContext(partialDomainObjectContext);
    }


    private static void convertContextUpdateToJava(
        RuntimeContext runtimeContext, Map<String, Object> contextUpdate, ContextModel contextModel
    ) throws ParseException
    {
        if (contextModel != null)
        {
            final DomainService domainService = runtimeContext.getDomainService();
            for (Map.Entry<String, ScopedPropertyModel> e : contextModel.getProperties().entrySet())
            {
                final String name = e.getKey();
                final ScopedPropertyModel model = e.getValue();

                if (contextUpdate.containsKey(name))
                {
                    final Object value = contextUpdate.get(name);

                    final PropertyConverter propertyConverter = domainService.getPropertyConverter
                        (model.getType());
                    final Object converted = propertyConverter.convertToJava(runtimeContext, value);

                    if (log.isDebugEnabled())
                    {
                        log.debug("Convert property '{}': {} => {}", name, value, converted);
                    }

                    contextUpdate.put(name, converted);
                }
            }
        }
    }


    public Map<String, String> getCurrentViewChanges()
    {
        return currentViewChanges;
    }


    @JSONProperty("currentEditorChanges")
    public void setCurrentViewChanges(Map<String, String> currentViewChanges)
    {
        this.currentViewChanges = currentViewChanges;
    }


    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + ": "
            + "objectContext = " + objectContext
            + ", contextUpdate = " + contextUpdate
            + ", currentViewChanges = " + currentViewChanges;

    }
}

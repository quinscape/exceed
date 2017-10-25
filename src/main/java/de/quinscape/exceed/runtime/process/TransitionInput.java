package de.quinscape.exceed.runtime.process;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ContextUpdate;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.Map;

/**
 * Contains context data for a transition invocation.
 * <p>
 * When one of the transitions defined in the current process view state is triggered from the outside, there domain
 * object data
 * to transmit along that transition this data contains:
 * <ul>
 *      <li>
 *          The domain object context for the transition execution which is extracted from the data cursor of TButton/Button
 *      </li>
 *      <li>
 *          A mapping definition mapping former query type prefixes to context variables
 *      </li>
 *      <li>
 *          Updates for context values the process allowed the user to edit via input fields or similar
 *      </li>
 * </ul>
 *
 * @see #parse(RuntimeContext, String, String, String)
 */
public class TransitionInput
{
    private final static Logger log = LoggerFactory.getLogger(TransitionInput.class);

    private Map<String, DomainObject> context;

    private Map<String, Object> contextUpdate;

    private Map<String, String> changedViewModels;

    private Map<String, String> mapping;

    private String stateName;


    public String getStateName()
    {
        return stateName;
    }


    public void setStateName(String stateName)
    {
        this.stateName = stateName;
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
    public static TransitionInput parse(RuntimeContext runtimeContext, String processName, String currentState, String
        json) throws ParseException
    {
        final DomainService domainService = runtimeContext.getDomainService();
        TransitionInput input = domainService.toDomainObject(TransitionInput.class, json);
        input.setStateName(currentState);

        final Process process = runtimeContext.getApplicationModel().getProcess(processName);

        if (process == null)
        {
            throw new IllegalStateException("Process '" + processName + "' not found.");
        }
        final View view = process.getView(currentState);
        if (view == null)
        {
            throw new IllegalStateException("View '" + currentState + "' not found in process '" + process.getName()
                + "'");
        }

        convertContextDomainObjects(runtimeContext, domainService, input);

        ContextUpdate.convertToJava(runtimeContext, process, view, input.getContextUpdate());
        return input;
    }


    private static void convertContextDomainObjects(
        RuntimeContext runtimeContext, DomainService domainService,
        TransitionInput input
    )
    {
        final Map<String, DomainObject> context = input.getContext();
        if (context != null)
        {
            for (Map.Entry<String, DomainObject> e : context.entrySet())
            {
                final DomainObject domainObject = e.getValue();
                final DomainType domainType = domainService.getDomainType(domainObject.getDomainType());

                for (DomainProperty domainProperty : domainType.getProperties())
                {
                    final PropertyType propertyType = PropertyType.get(runtimeContext, domainProperty);
                    final String propertyName = domainProperty.getName();

                    final Object converted = propertyType.convertToJava(
                        runtimeContext,
                        domainObject.getProperty(
                            propertyName
                        )
                    );

                    domainObject.setProperty(propertyName, converted);
                }

                if (domainObject.getId() != null)
                {
                    DomainUtil.merge(runtimeContext, domainObject);
                }
            }
        }
    }


    public Map<String, String> getChangedViewModels()
    {
        return changedViewModels;
    }


    public void setChangedViewModels(Map<String, String> changedViewModels)
    {
        this.changedViewModels = changedViewModels;
    }


    public Map<String, DomainObject> getContext()
    {
        return context;
    }


    @JSONTypeHint(GenericDomainObject.class)
    public void setContext(Map<String, DomainObject> context)
    {
        this.context = context;
    }


    public Map<String, String> getMapping()
    {
        return mapping;
    }


    public void setMapping(Map<String, String> mapping)
    {
        this.mapping = mapping;
    }


    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + ": "
            + ", contextUpdate = " + contextUpdate
            + ", changedViewModels = " + changedViewModels;

    }
}

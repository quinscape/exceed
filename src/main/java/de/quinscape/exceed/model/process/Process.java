package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.DocumentedCollection;
import de.quinscape.exceed.model.annotation.DocumentedSubTypes;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeLocationModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exceed process model
 *
 */
public class Process
    extends AbstractTopLevelModel
    implements AutoVersionedModel, ScopeLocationModel
{

    private final static Logger log = LoggerFactory.getLogger(Process.class);

    private Transition startTransition;

    private Map<String, ProcessState> states;

    private ContextModel contextModel;

    private ApplicationModel applicationModel;
    
    /**
     * Process states of the process.
     *
     * @return
     */
    @DocumentedCollection(keyDesc = "stateName")
    @DocumentedSubTypes({
        ViewState.class,
        DecisionState.class
    })
    public Map<String, ProcessState> getStates()
    {
        if (states == null)
        {
            return Collections.emptyMap();
        }

        return states;
    }


    @JSONTypeHint(ProcessState.class)
    public void setStates(Map<String, ProcessState> states)
    {
        this.states = states;
    }


    /**
     * Transition be executed when the process is entered.
     *
     */
    public Transition getStartTransition()
    {
        return startTransition;
    }


    public void setStartTransition(Transition startTransition)
    {
        startTransition.setFrom("start");
        startTransition.setName("start");
        this.startTransition = startTransition;
    }


    public void postProcess(ApplicationModel applicationModel)
    {
        this.applicationModel = applicationModel;

        for (Map.Entry<String, ProcessState> entry : states.entrySet())
        {
            ProcessState state = entry.getValue();
            state.setName(entry.getKey());
            state.setProcess(this);
        }

        final Map<String, ScopedPropertyModel> properties;
        if (contextModel == null)
        {
            contextModel = new ContextModel();
            properties = new HashMap<>();
            contextModel.setProperties(properties);
        }
        else
        {
            properties = contextModel.getProperties();
        }

        if (!properties.containsKey(ProcessContext.CURRENT))
        {
            properties.put(
                ProcessContext.CURRENT,
                DomainProperty.builder()
                    .withName(ProcessContext.CURRENT)
                    // any domain type will do
                    .withType(PropertyType.DOMAIN_TYPE, null)
                    .buildScoped()
                );
        }
//        if (!properties.containsKey(ProcessContext.DOMAIN_CONTEXT))
//        {
//            properties.put(ProcessContext.DOMAIN_CONTEXT, DomainProperty.builder()
//                .withName(ProcessContext.DOMAIN_CONTEXT)
//                .withType(PropertyType.DATA_GRAPH)
//                .buildScoped()
//            );
//        }
        
        applicationModel.getMetaData().createPropertyTypes(contextModel);
    }

    
    @JSONProperty(ignore = true)
    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }

    public View getView(String localName)
    {
        if (applicationModel == null)
        {
            throw new IllegalStateException("No applicationModel");
        }

        return applicationModel.getView(getProcessStateName(localName));
    }

    public static String getProcessStateName(String processName, String localName)
    {
        return processName + "/" + localName;
    }

    public String getProcessStateName(String localName)
    {
        return getProcessStateName(getName(), localName);
    }


    /**
     * Process context. Each property in it will exist once per execution of the process. The server remembers past
     * process contexts to continue work at this point when the user returns to another view with browser history
     * navigation.
     */
    public ContextModel getContextModel()
    {
        return contextModel;
    }


    @JSONProperty("context")
    public void setContextModel(ContextModel contextModel)
    {
        this.contextModel = contextModel;
    }

    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }

    @Override
    @Internal
    public String getScopeLocation()
    {
        return getName() + "/start";
    }


}

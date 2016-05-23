package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;

/**
 * Exceed process model
 *
 */
public class Process
    extends TopLevelModel
    implements AutoVersionedModel
{

    private final static Logger log = LoggerFactory.getLogger(Process.class);

    private String version;

    private Transition startTransition;

    private Map<String, ProcessState> states;

    private ContextModel context;

    private ApplicationModel applicationModel;


    /**
     * Auto-generated version identifier.
     *
     * @return
     */
    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(String version)
    {
        this.version = version;
    }


    /**
     * Returns th
     * @return
     */
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


    @PostConstruct
    public void checkTransitions()
    {
        for (Map.Entry<String, ProcessState> entry : states.entrySet())
        {
            ProcessState state = entry.getValue();
            state.setName(entry.getKey());
            state.validate(this);
        }
    }


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


    public void setApplicationModel(ApplicationModel applicationModel)
    {
        this.applicationModel = applicationModel;
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

        return applicationModel.getView(getProcessViewName(this.getName(), localName));
    }

    public static String getProcessViewName(String processName, String localName)
    {
        return processName + "/" + localName;
    }


    public ContextModel getContext()
    {
        return context;
    }


    public void setContext(ContextModel context)
    {
        this.context = context;
    }
}

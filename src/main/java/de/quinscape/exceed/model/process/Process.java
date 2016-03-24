package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.action.ActionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONTypeHint;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Process
    extends TopLevelModel
    implements AutoVersionedModel
{

    private final static Logger log = LoggerFactory.getLogger(Process.class);

    private String version;

    private String start;
    private List<ActionModel> startActions;

    private Map<String, ProcessState> states;

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

    public String getStart()
    {
        return start;
    }


    public void setStart(String start)
    {
        this.start = start;
    }


    public List<ActionModel> getStartActions()
    {
        if (startActions == null)
        {
            return Collections.emptyList();
        }

        return startActions;
    }


    public void setStartActions(List<ActionModel> startActions)
    {
        this.startActions = startActions;
    }
}

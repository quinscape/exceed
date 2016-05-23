package de.quinscape.exceed.runtime.process;

import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.util.IdCounter;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class ProcessExecutionState
{
    private static final String STATE_MAP_KEY = ProcessExecutionState.class.getName() + ":stateMap";

    private final String id;

    private final static IdCounter COUNTER = new IdCounter();

    private final ProcessExecution execution;

    private final String currentState;

    private final ScopedContext scopedContext;

    public ProcessExecutionState(ProcessExecution execution, ScopedContext scopedContext)
    {
        this(execution, scopedContext, null);
    }

    public ProcessExecutionState(ProcessExecution execution, ScopedContext scopedContext, String currentState)
    {
        this.id = COUNTER.nextId();
        this.execution = execution;
        this.scopedContext = scopedContext;
        this.currentState = currentState;

        execution.setState(this);
    }

    public String getId()
    {
        return id;
    }


    public String getCurrentState()
    {
        return currentState;
    }

    public ProcessExecution getExecution()
    {
        return execution;
    }

    public ScopedContext getScopedContext()
    {
        return scopedContext;
    }


    public void register(HttpSession session)
    {
        Map<String,ProcessExecutionState> states = (Map<String, ProcessExecutionState>) session.getAttribute(STATE_MAP_KEY);
        if (states == null)
        {
            states = new HashMap<>();
        }
        states.put(this.id, this);
        session.setAttribute(STATE_MAP_KEY, states);
    }

    public static ProcessExecutionState lookup(HttpSession session, String id)
    {
        Map<String,ProcessExecutionState> states = (Map<String, ProcessExecutionState>) session.getAttribute(STATE_MAP_KEY);
        if (states == null)
        {
            return null;
        }
        return states.get(id);

    }
}

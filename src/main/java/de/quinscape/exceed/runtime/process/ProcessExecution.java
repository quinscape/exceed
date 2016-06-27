package de.quinscape.exceed.runtime.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessExecution
    implements Serializable
{
    private final String appName;

    private final String processName;

    private final List<String> states;

    private String currentState;


    public ProcessExecution(String appName, String processName)
    {
        this.appName = appName;
        this.processName = processName;
        this.states = new ArrayList<>();
    }

    void setState(ProcessExecutionState processExecutionState)
    {
        String stateId = processExecutionState.getId();
        this.states.add(stateId);
        this.currentState = stateId;
    }

    public String getAppName()
    {
        return appName;
    }

    public String getProcessName()
    {
        return processName;
    }


    public List<String> getStates()
    {
        return states;
    }


    public String getCurrentState()
    {
        return currentState;
    }
}

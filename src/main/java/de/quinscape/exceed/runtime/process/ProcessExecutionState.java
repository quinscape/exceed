package de.quinscape.exceed.runtime.process;

import de.quinscape.exceed.model.process.Process;

import java.util.HashMap;
import java.util.Map;

public class ProcessExecutionState
{
    private final Map<String, Object> processParams;

    private final String processName;

    private final String currentState;


    public ProcessExecutionState(Process process, Map<String, Object> processParams)
    {
        this.processName = process.getName();
        this.processParams = new HashMap<>(processParams);

        currentState = "start";
    }


    public Map<String, Object> getProcessParams()
    {
        return processParams;
    }


    public String getProcessName()
    {
        return processName;
    }


    public String getCurrentState()
    {
        return currentState;
    }
}

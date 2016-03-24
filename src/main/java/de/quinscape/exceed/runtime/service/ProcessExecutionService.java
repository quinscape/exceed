package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessExecutionService
{
    @Autowired
    private ActionService actionService;

    public ProcessExecutionState start(RuntimeContext runtimeContext, Process process)
    {

        ProcessExecutionState state = new ProcessExecutionState(process, runtimeContext.getLocationParams());
        Transition transition = process.getStartTransition();

        return resume(runtimeContext, state, transition);
    }


    private ProcessExecutionState resume(RuntimeContext runtimeContext, ProcessExecutionState state, Transition
        transition)
    {
        if (!transition.getFrom().equals(state.getCurrentState()))
        {
            throw new ProcessExecutionException("State mismatch: " + transition.getFrom() + " != " + state.getCurrentState());
        }

        //executeActionList(runtimeContext, transition.getActions());


        return null;
    }
}

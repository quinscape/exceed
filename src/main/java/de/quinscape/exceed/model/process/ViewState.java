package de.quinscape.exceed.model.process;

import org.svenson.JSONTypeHint;

import java.util.Map;

public class ViewState
    extends ProcessState
{
    private Map<String,Transition> transitions;

    public Map<String, Transition> getTransitions()
    {
        return transitions;
    }

    @JSONTypeHint(Transition.class)
    public void setTransitions(Map<String, Transition> transitions)
    {
        for (Map.Entry<String, Transition> entry : transitions.entrySet())
        {
            String name = entry.getKey();
            Transition transition = entry.getValue();
            transition.setName(name);
            transition.setFrom(this.getName());
        }
        this.transitions = transitions;
    }


    @Override
    public void validate(Process process)
    {
        Map<String, Transition> transitions = getTransitions();
        if (transitions != null)
        {
            for (Map.Entry<String, Transition> entry : transitions.entrySet())
            {
                String to = entry.getValue().getTo();
                if (to == null)
                {
                    throw new IllegalStateException("Process '" + process.getName() + "':  Transition '" + entry.getKey() + "' has no target process state");
                }
                if (!process.getStates().containsKey(to))
                {
                    throw new IllegalStateException("Process '" + process.getName() + "':  Transition '" + entry.getKey() + "' references non-existing process-state '" + to + "'");
                }
            }
        }
    }
}

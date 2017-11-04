package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.annotation.DocumentedCollection;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONTypeHint;

import java.util.Map;

/**
 * A view state within a process. Corresponds with a process view model.
 */
public class ViewState
    extends ProcessState
{
    private Map<String,Transition> transitions;

    public Map<String, Transition> getTransitions()
    {
        return transitions;
    }


    /**
     * Transitions for this view state.
     *
     */
    @JSONTypeHint(Transition.class)
    @DocumentedCollection(keyDesc = "transitionName")
    public void setTransitions(Map<String, Transition> transitions)
    {
        this.transitions = transitions;
    }

    @Override
    public void postProcess()
    {
        final Process process = getProcess();
        Map<String, Transition> transitions = getTransitions();
        if (transitions != null)
        {
//            final AssignmentReplacementVisitor visitor = new AssignmentReplacementVisitor(process
//                .getApplicationModel(), process.getView(this.getName()).getName());

            for (Map.Entry<String, Transition> entry : transitions.entrySet())
            {
                Transition transition = entry.getValue();
                String to = transition.getTo();
                final String transitionName = entry.getKey();
                if (to == null)
                {
                    throw new InconsistentModelException("Process '" + process.getName() + "':  Transition '" + transitionName + "' has no target process state");
                }
                if (!process.getStates().containsKey(to))
                {
                    throw new InconsistentModelException("Process '" + process.getName() + "':  Transition '" + transitionName + "' references non-existing process-state '" + to + "'");
                }

                transition.setFrom(getName());
                transition.setName(transitionName);

                final Confirmation confirmation = transition.getConfirmation();
                if (confirmation != null)
                {
                    if (confirmation.getMessageValue() == null)
                    {
                        throw new InconsistentModelException("Transition confirmation needs a message: " + this);
                    }
                }
            }
        }
    }
}

/**
 * { popCursor(current) }
 *
 *
 */

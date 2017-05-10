package de.quinscape.exceed.model.process;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.model.annotation.DocumentedMapKey;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.AssignmentReplacementVisitor;
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


    /**
     * Transitions for this view state.
     *
     */
    @JSONTypeHint(Transition.class)
    @DocumentedMapKey("transitionName")
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
            final AssignmentReplacementVisitor visitor = new AssignmentReplacementVisitor(process
                .getApplicationModel(), process.getView(this.getName()).getName());

            for (Map.Entry<String, Transition> entry : transitions.entrySet())
            {
                Transition transition = entry.getValue();
                String to = transition.getTo();
                final String transitionName = entry.getKey();
                if (to == null)
                {
                    throw new IllegalStateException("Process '" + process.getName() + "':  Transition '" + transitionName + "' has no target process state");
                }
                if (!process.getStates().containsKey(to))
                {
                    throw new IllegalStateException("Process '" + process.getName() + "':  Transition '" + transitionName + "' references non-existing process-state '" + to + "'");
                }

                transition.setFrom(getName());
                transition.setName(transitionName);

                try
                {
                    final ASTExpression actionAST = ExpressionParser.parse(transition.getAction());
                    if (actionAST != null)
                    {
                        actionAST.jjtAccept(visitor, null);
                        transition.setActionAST(actionAST);
                    }
                }
                catch (Exception e)
                {
                    throw new ExceedRuntimeException("Error postprocessing " + transition + " in " + this, e);
                }
            }
        }
    }
}

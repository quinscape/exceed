package de.quinscape.exceed.model.process;

import org.svenson.JSONTypeHint;

import java.util.List;

public class DecisionState
    extends ProcessState
{
    private List<DecisionModel> decisions;


    public List<DecisionModel> getDecisions()
    {
        return decisions;
    }

    @JSONTypeHint(DecisionModel.class)
    public void setDecisions(List<DecisionModel> decisions)
    {
        for (int i = 0; i < decisions.size(); i++)
        {
            DecisionModel decision = decisions.get(i);
            if (decision.getExpressionAST() == null)
            {
                throw new IllegalArgumentException("Expression not set on decision: " + decision);
            }
            Transition transition = decision.getTransition();
            if (transition == null)
            {
                throw new IllegalArgumentException("Transition not set on decision: " + decision);
            }

            transition.setName("t-" + i);
            transition.setFrom(getName());
        }
        this.decisions = decisions;
    }


    @Override
    public void validate(Process process)
    {
        for (DecisionModel decision : decisions)
        {
            Transition transition = decision.getTransition();
            String to = transition.getTo();
            if (to == null)
            {
                throw new IllegalStateException("Process '" + process.getName() + "':  Transition for '" + decision.getExpression() + "' has no target process state");
            }
            if (!process.getStates().containsKey(to))
            {
                throw new IllegalStateException("Process '" + process.getName() + "':  Transition for '" + decision.getExpression() + "' references non-existing process-state '" + to + "'");
            }
        }
    }
}

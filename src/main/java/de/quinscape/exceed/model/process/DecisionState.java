package de.quinscape.exceed.model.process;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;

public class DecisionState
    extends ProcessState
{
    private List<DecisionModel> decisions;

    private Transition defaultTransition;

    public List<DecisionModel> getDecisions()
    {
        return decisions;
    }

    @JSONProperty(priority = 10)
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

            transition.setName("t" + i);
        }


        this.decisions = decisions;
    }


    public Transition getDefaultTransition()
    {
        return defaultTransition;
    }


    public void setDefaultTransition(Transition defaultTransition)
    {
        this.defaultTransition = defaultTransition;
    }


    @Override
    public void validate(Process process)
    {
        final String decisionStateName = getName();
        defaultTransition.setFrom(decisionStateName);
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

            transition.setFrom(decisionStateName);
        }

        Transition defaultTransition = getDefaultTransition();
        if (defaultTransition == null)
        {
            throw new IllegalStateException("No default transition given for decision state");
        }
        String to = defaultTransition.getTo();
        if (to == null)
        {
            throw new IllegalStateException(
                "Process '" + process.getName() + "':  Default Transition for decision state '" + this.getName() + "' has no target process state");
        }
        if (!process.getStates().containsKey(to))
        {
            throw new IllegalStateException(
                "Process '" + process.getName() + "':  " +
                "Default Transition for decision " + "state '\" + this.getName() + \"' references non-existing process-state '" + to + "'");
        }
    }
}

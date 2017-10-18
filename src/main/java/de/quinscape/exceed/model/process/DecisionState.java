package de.quinscape.exceed.model.process;

import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;

/**
 * A decision state within a process,
 * <p>
 *     A series of decisions is considered in definition order, each having an expression attribute taking a value expression
 *     and a transition to execute in case that expression evaluates to <code>true</code>.
 * </p>
 * <p>
 *     If none of the decision transitions is taken, the default transition is used.
 * </p>
 */
public class DecisionState
    extends ProcessState
{
    private static final String DEFAULT_TRANSITION_NAME = "default";

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
        defaultTransition.setName(DEFAULT_TRANSITION_NAME);
        this.defaultTransition = defaultTransition;
    }


    @Override
    public void postProcess()
    {
        final Process process = getProcess();
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

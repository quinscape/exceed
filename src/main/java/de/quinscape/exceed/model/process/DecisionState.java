package de.quinscape.exceed.model.process;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.AssignmentReplacementVisitor;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;

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

        final AssignmentReplacementVisitor visitor = new AssignmentReplacementVisitor(process.getApplicationModel(), Process.getProcessViewName(process.getName(), getName()));

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
            parseTransitionAction(visitor, transition);
        }

        Transition defaultTransition = getDefaultTransition();

        if (defaultTransition == null)
        {
            throw new IllegalStateException("No default transition given for decision state");
        }

        parseTransitionAction(visitor, defaultTransition);

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


    public void parseTransitionAction(AssignmentReplacementVisitor visitor, Transition transition)
    {
        try
        {
            final ASTExpression actionAST = ExpressionParser.parse(transition.getAction());
            if (actionAST != null)
            {
                actionAST.jjtAccept(visitor, null);
                transition.setActionAST(actionAST);
            }
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException("Error parsing transition action expression", e);
        }
    }
}

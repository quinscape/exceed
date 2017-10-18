package de.quinscape.exceed.model.process;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONProperty;

import java.util.Collection;

/**
 * A transition within a process
 */
public class Transition
{

    public final static String CURRENT = "current";

    private String name;

    private String from;

    private String to;

    /**
     * Raw action object: String or Collection of strings
     */
    private Object rawAction;

    /**
     * Parsed expression value for action
     */
    private ExpressionValue action;

    private String description;

    private boolean discard = false;

    private Confirmation confirmation;

    /**
     * Name of the transition. Is automatically set when the transition is added to its state.
     */
    @Internal
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public void setFrom(String from)
    {
        this.from = from;
    }


    /**
     * Source state of this transition. Is automatically set then the transition is added to its state.
     */
    @JSONProperty(ignore = true)
    public String getFrom()
    {
        return from;
    }


    /**
     * Target state for this transition.
     */
    public String getTo()
    {
        return to;
    }


    public void setTo(String to)
    {
        this.to = to;
    }


    /**
     * Action(s) to execute for this transition.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true)
    public Object getAction()
    {
        return rawAction;
    }

    public String getActionString()
    {
        return action != null ? action.getValue() : null;
    }

    public void setAction(Object actionValue)
    {
        final String action;
        if (actionValue instanceof Collection)
        {
            action = Util.join((Collection<?>) actionValue, " ");
        }
        else if (actionValue instanceof String)
        {
            action = (String)actionValue;
        }
        else
        {
            throw new InconsistentModelException("Invalid transition action value: " + actionValue);
        }
        this.action = ExpressionValue.forValue(action, true);
        this.rawAction = actionValue;
    }


    /**
     * True if the transition discards user input, e.g. a "Cancel" transition.
     */
    public boolean isDiscard()
    {
        return discard;
    }


    public void setDiscard(boolean discard)
    {
        this.discard = discard;
    }


    @JSONProperty(ignore = true)
    public ASTExpression getActionAST()
    {
        return action != null ? action.getAstExpression() : null;
    }


    @JSONProperty(ignore = true)
    public ExpressionValue getActionValue()
    {
        return action;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Description of the transition.
     */
    public String getDescription()
    {
        return description;
    }


    public Confirmation getConfirmation()
    {
        return confirmation;
    }


    public void setConfirmation(Confirmation confirmation)
    {
        this.confirmation = confirmation;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", '" + from + "' => '" + to + "'"
            ;
    }
}

package de.quinscape.exceed.model.process;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.model.annotation.Internal;
import org.svenson.JSONProperty;

/**
 * A transition within a process
 */
public class Transition
{
    private String name;

    private String from;

    private String to;

    private String action;

    private String description;

    private ASTExpression actionAST;

    private boolean discard = false;

    private boolean mergeContext = true;


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
     * @return
     */
    @JSONProperty(ignoreIfNull = true)
    public String getAction()
    {
        return action;
    }


    public void setAction(String action)
    {
        this.action = action;
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
        return actionAST;
    }


    public void setActionAST(ASTExpression actionAST)
    {
        this.actionAST = actionAST;
    }


    /**
     * If set to <code>false</code>, the context transitioned with the executing transition is not merged
     * with an existing entity with the same id. The default is <code>true</code> which means that the
     * context object will be merged by default.
     */
    public boolean isMergeContext()
    {
        return mergeContext;
    }


    public void setMergeContext(boolean mergeContext)
    {
        this.mergeContext = mergeContext;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * Description of the transition. 
     * @return
     */
    public String getDescription()
    {
        return description;
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

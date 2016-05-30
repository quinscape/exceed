package de.quinscape.exceed.model.process;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.svenson.JSONProperty;

public class Transition
{
    private String name;

    private String from;

    private String to;

    private String action;

    private ASTExpression actionAST;

    private boolean discard = false;

    private boolean mergeContext = true;

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


    @JSONProperty(ignore = true)
    public String getFrom()
    {
        return from;
    }


    public String getTo()
    {
        return to;
    }


    public void setTo(String to)
    {
        this.to = to;
    }


    @JSONProperty(ignoreIfNull = true)
    public String getAction()
    {
        return action;
    }


    public void setAction(String action)
    {
        this.action = action;
        try
        {
            this.actionAST = ExpressionUtil.handleAssignmentAction(ExpressionParser.parse(action));
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


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


    public boolean isMergeContext()
    {
        return mergeContext;
    }


    public void setMergeContext(boolean mergeContext)
    {
        this.mergeContext = mergeContext;
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

package de.quinscape.exceed.expression;

public abstract class LogicalOperatorNode
    extends OperatorNode
{
    public LogicalOperatorNode(int i)
    {
        super(i);
    }


    public LogicalOperatorNode(ExpressionParser p, int i)
    {
        super(p, i);
    }
}

package de.quinscape.exceed.expression;

public abstract class  ComparatorNode
    extends OperatorNode
{
    public ComparatorNode(int i)
    {
        super(i);
    }


    public ComparatorNode(ExpressionParser p, int i)
    {
        super(p, i);
    }
}

package de.quinscape.exceed.expression;

public abstract class ArithmeticOperatorNode
    extends OperatorNode
{
    public ArithmeticOperatorNode(int i)
    {
        super(i);
    }


    public ArithmeticOperatorNode(ExpressionParser p, int i)
    {
        super(p, i);
    }
}

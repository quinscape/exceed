package de.quinscape.exceed.expression;

public abstract class OperatorNode
    extends SimpleNode
{
    private Operator operator;


    public OperatorNode(int i)
    {
        super(i);
    }


    public OperatorNode(ExpressionParser p, int i)
    {
        super(p, i);
    }


    public Operator getOperator()
    {
        return operator;
    }


    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }
}

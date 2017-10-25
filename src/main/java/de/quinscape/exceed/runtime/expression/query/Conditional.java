package de.quinscape.exceed.runtime.expression.query;

public class Conditional
{
    private final boolean condition;

    public Conditional(Boolean condition)
    {

        this.condition = condition;
    }

    public boolean isTrue()
    {
        return condition;
    }
}

package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.Node;

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

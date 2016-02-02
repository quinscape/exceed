package de.quinscape.exceed.expression;

public enum Operator
{
    EQUALS("=="), NOT_EQUALS("!="),

    LESS("<"), LESS_OR_EQUALS("<="), GREATER(">"), GREATER_OR_EQUALS(">="),

    NOT("!"),

    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"),

    AND("&&"), OR("||");

    private final String stringOp;


    Operator(String stringOp)
    {
        this.stringOp = stringOp;
    }


    public String getAsString()
    {
        return stringOp;
    }
}

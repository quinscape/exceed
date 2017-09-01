package de.quinscape.exceed.runtime.action;

public class MethodCall
{
    private final String method;

    private final Object[] args;


    public MethodCall(String method, Object[] args)
    {
        this.method = method;
        this.args = args;
    }


    public String getMethod()
    {
        return method;
    }

    public Object getArg(int index)
    {
        return args[index];
    }

    public int getArgsCount()
    {
        return args.length;
    }
}

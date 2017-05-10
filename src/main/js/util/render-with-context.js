export default function(children, context)
{
    if (typeof children === "function")
    {
        return children(context);
    }
    else
    {
        return children;
    }
}

/**
 * Member binding helper
 *
 * @param ctx       local "this"
 * @param name      prototype function to be bound to that "this"
 */
export default function(ctx, name)
{
    var fn = ctx[name];
    if (typeof fn !== "function")
    {
        throw new Error("Cannot bind non-function " + fn);
    }
    ctx[name] = fn.bind(ctx);
}

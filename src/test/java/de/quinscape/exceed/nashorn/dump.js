
function dump(obj)
{
    var s = "";
    var type = typeof obj;
    s += (obj + "typeof = " + type);
    if (type !== "string" && type !== "number" && type !== "boolean")
    {
        for (var n in obj)
        {
            if (obj.hasOwnProperty(n))
            {
                s += ("    " + n + " = " + obj[n] + "\n");
            }
        }
    }
    return s;
}

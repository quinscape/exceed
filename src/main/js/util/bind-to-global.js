
const globalEval = eval;
const global = globalEval("this");

export default function(obj)
{
    for (let name in obj)
    {
        if (obj.hasOwnProperty(name))
        {
            Object.defineProperty(
                global,
                name,
                {
                    value: obj[name],
                    enumerable: true
                }
            );
        }
    }
    return global;
}

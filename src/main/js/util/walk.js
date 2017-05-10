const literalRE = /^[a-z_][a-z0-9_]*$/gi;

function createPathError(msg, obj, path, index)
{
    let pathDesc = "";
    let remaining = "";

    for (let i = 0; i < path.length; i++)
    {

        let p = path[i];

        let part;
        if (typeof p === "string" && literalRE.test(p))
        {
            part = "." + p;
        }
        else
        {
            part = "[" + JSON.stringify(p) + "]"
        }

        pathDesc += part;
        if (i > index)
        {
            remaining += part;
        }
    }

    const message = msg + " 'obj" + pathDesc + "' at '" + remaining;
    console.error(message, obj);

    return new Error(message);
}

/**
 * Traverses the given object along the given path of prop values.
 *
 * @param obj   {object} root of object graph
 * @param path  {Array} Array of properties to access to get to the next object
 * @returns {*} result
 */
export default function walk(obj, path)
{
    //console.log("WALK", obj, path);

    const root = obj;

    let i;
    const len = path.length;
    for (i = 0; i < len; i++)
    {
        //console.log("W", obj, path[i]);
        const current = obj;
        const prop = path[i];
        obj = obj[prop];

        // DEV: Additional safety checking to catch errors earlier and with usable description
        if (__DEV)
        {
            // is the current object falsy or not an object?
            if (!obj || typeof obj !== "object")
            {
                // is the current base an array and the index was too large?
                if (typeof prop === "number" && isArray(current) && prop >= current.length)
                {
                    throw createPathError("Index out of bounds for ", root, path, i - 1);
                }

                // are we not done walking our path?
                if (i < len - 1)
                {
                    throw createPathError("Encountered non-walkable object " + obj + " in chain", root, path, i);
                }
            }
        }
    }
    return obj;
}

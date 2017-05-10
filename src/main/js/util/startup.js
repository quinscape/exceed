
export function evaluateEmbedded(elemId, mediaType)
{
    let elem = document.getElementById(elemId);
    if (!elem || elem.getAttribute("type") !== mediaType)
    {
        throw new Error("#" + elemId + " is not a script of type '" + mediaType + "': " + elem);
    }

    return JSON.parse(elem.innerHTML);
}

export function findBundles(scriptResourcePath)
{
    return Array.prototype.slice.call(
        document.querySelectorAll("head script[src]")
    )
        .map(elem =>
        {
            const path = elem.src;

            const pos = path.indexOf(scriptResourcePath);
            if (pos >= 0)
            {
                return path.substring(pos + scriptResourcePath.length);
            }
            return path;
        });
}

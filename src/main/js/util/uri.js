"use strict";

var contextPath = null;

function evaluateParams(params)
{
    var p = "";
    if (params)
    {
        var sep = "?";
        for (var name in params)
        {
            if (params.hasOwnProperty(name))
            {
                p += sep + encodeURIComponent(name) + "=" + encodeURIComponent(params[name]);
                sep = "&";
            }
        }
    }
    return p;
}

function replacePathVariables(location, params)
{
   return location.replace(/{([a-z]+)}/g, function (match, name, offset, str)
    {
        var value = params[name];
        if (value === undefined)
        {
            throw new Error("Undefined path variable '" + name + "' in '" + location + "'");
        }
        delete params[name];
        return value;
    });
}
function uri(location, params)
{
    location = replacePathVariables(location, params);

    if (contextPath === null)
    {
        if (typeof document !== "undefined")
        {
            contextPath = document.body && document.body.dataset && document.body.dataset.contextPath;

            if (typeof contextPath !== "string")
            {
                throw new Error("Context path not initialized");
            }
            //console.log("context-path from body[data-context-path] = " + contextPath);
        }
    }

    var result = contextPath + location + evaluateParams(params);

    //console.log("URI:", result);

    return result;
}

uri._init_context_path  = function(cp)
{
    if (!contextPath)
    {
        contextPath = cp;
    }
};

module.exports = uri;

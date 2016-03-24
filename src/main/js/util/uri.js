"use strict";

var sys = require("../sys");

function evaluateParams(params, usedInPath)
{
    var p = "";
    if (params)
    {
        var sep = "?";
        for (var name in params)
        {
            if (params.hasOwnProperty(name) && !usedInPath[name])
            {
                var value = params[name];
                if (value !== undefined)
                {
                    p += sep + encodeURIComponent(name) + "=" + encodeURIComponent(value);
                    sep = "&";
                }
            }
        }
    }
    return p;
}


function replacePathVariables(location, params, usedInPath)
{
   return location.replace(/{([a-z]+)}/g, function (match, name, offset, str)
    {
        var value = params[name];
        if (value === undefined)
        {
            throw new Error("Undefined path variable '" + name + "' in '" + location + "'");
        }
        usedInPath[name] = true;
        return value;
    });
}
function uri(location, params)
{
    var usedInPath = {};
    location = replacePathVariables(location, params, usedInPath);

    var result = sys.contextPath + location + evaluateParams(params, usedInPath);

    //console.log("URI:", result);

    return result;
}

module.exports = uri;

"use strict";

var sys = require("../sys");

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

    var result = sys.contextPath + location + evaluateParams(params);

    //console.log("URI:", result);

    return result;
}

module.exports = uri;

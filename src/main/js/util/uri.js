"use strict";

var sys = require("../sys");
var extend = require("extend");

var url = require("url");

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
        var value = params && params[name];
        if (value === undefined)
        {
            throw new Error("Undefined path variable '" + name + "' in '" + location + "'");
        }
        usedInPath[name] = true;
        return value;
    });
}

function uri(location, params, containsContextPath)
{
    var usedInPath = {};

    var hPos = location.indexOf("#");
    if (hPos >=0)
    {
        location = location.substring(0, hPos);
    }
    var qPos = location.indexOf("?");
    if (qPos >= 0)
    {
        var current = url.parse(location, true);
        params = extend(current.query, params);
        location = location.substring(0, qPos);
    }

    location = replacePathVariables(location, params, usedInPath);

    var result = (containsContextPath ? "" : sys.contextPath) + location + evaluateParams(params, usedInPath);

    //console.log("URI:", result);

    return result;
}

module.exports = uri;

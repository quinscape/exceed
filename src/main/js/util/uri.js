"use strict";

const sys = require("../sys");
const assign = require("object-assign");

const url = require("url");

function evaluateParams(params, usedInPath)
{
    let p = "";
    if (params)
    {
        let sep = "?";
        for (let name in params)
        {
            if (params.hasOwnProperty(name) && !usedInPath[name])
            {
                const value = params[name];
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
   return location.replace(/{([0-9a-z_]+)\??}/gi, function (match, name, offset, str)
    {
        const value = params && params[name];
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
    const usedInPath = {};

    location = replacePathVariables(location, params, usedInPath);

    const hPos = location.indexOf("#");
    if (hPos >= 0)
    {
        location = location.substring(0, hPos);
    }
    const qPos = location.indexOf("?");
    if (qPos >= 0)
    {
        const current = url.parse(location, true);
        params = assign(current.query, params);
        location = location.substring(0, qPos);
    }


    const result = (containsContextPath ? "" : sys.contextPath) + location + evaluateParams(params, usedInPath);

    //console.log("URI:", result);

    return result;
}

module.exports = uri;

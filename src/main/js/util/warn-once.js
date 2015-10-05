"use strict";

var cando = require("../cando");

const MAP_KEY = "warnOnce:warnings";

/**
 * Warns a user once per session about something with an alert,
 *
 * @param name      {string?} key for this message
 * @param message   {string} message
 */
module.exports = function(name, message)
{
    if (cando.sessionStorage)
    {
        console.log("can do sessionStorage");

        message = message || name;

        var key = "warnOnce:" + name;

        var warnings = JSON.parse(sessionStorage.getItem(MAP_KEY));
        if (warnings)
        {
            console.log("map present");
            if (warnings[key])
            {
                console.log("warning registered");
                return true;
            }
        }
        else
        {
            console.log("no warnings");
            // no warnings map -> create one with our entry
            warnings = {  };
        }

        warnings[key] = true;

        console.log("store %o", warnings);
        sessionStorage.setItem(MAP_KEY, JSON.stringify(warnings));
    }
    alert(message)
};

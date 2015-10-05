"use strict";

const XMLHttpFactories = [
    function ()
    {
        return new XMLHttpRequest()
    },
    function ()
    {
        return new ActiveXObject("Msxml2.XMLHTTP")
    },
    function ()
    {
        return new ActiveXObject("Msxml3.XMLHTTP")
    },
    function ()
    {
        return new ActiveXObject("Microsoft.XMLHTTP")
    }
];

var startAt = 0;

module.exports = function()
{
    var xmlhttp = false;
    for (var i = startAt; i < XMLHttpFactories.length; i++)
    {
        try
        {
            xmlhttp = XMLHttpFactories[i]();
        }
        catch (e)
        {
            continue;
        }
        // remember succesful index to start with it the next time
        startAt = i;
        break;
    }
    return xmlhttp;
};

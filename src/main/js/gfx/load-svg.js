"use strict";

import ajax from "./../service/ajax"
import cando from "../cando"
import parseXML from "./../util/parseXML"

var Promise = require("es6-promise-polyfill").Promise;

var nameSpacesToClean = {
    "http://www.inkscape.org/namespaces/inkscape" : 0,
    "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" : 1
};

function getNSRegExp(elem)
{
    var nsName = [];

    var attrs = elem.attributes;
    var len = attrs.length;
    for (var i = 0; i < len; i++)
    {
        var attr = attrs[i];
        var name = attr.name;

        var idx = nameSpacesToClean[attr.value];
        if (name.indexOf("xmlns:") === 0 && typeof idx === "number")
        {
            nsName[idx] = name.substring(6);

            elem.removeAttribute(name);
            i--;
            len--;
        }
    }

    var pattern = "^(" + nsName.join("|") + "):";
//    console.log("pattern = ", pattern);
    return new RegExp(pattern);
}
function removePrefixedAttributes(elem, re)
{
    if ( elem.hasAttributes())
    {
        if (!re.test(elem.nodeName))
        {
        } else
        {
            var next = elem.nextElementSibling;
            elem.parentNode.removeChild(elem);

            if (!next)
            {
                return
            }
            elem = next;
        }

        var attrs = elem.attributes;
        var len = attrs.length;
        for (var i = 0; i < len; i++)
        {
            var attr = attrs[i];

            if (re.test(attr.name))
            {
                elem.removeAttribute(attr.name);
                i--;
                len--;
            }
        }
    }

    var nextSibling = elem.nextElementSibling;
    if (nextSibling)
    {
        removePrefixedAttributes(nextSibling, re);
    }

    var firstChild = elem.firstElementChild;
    if (firstChild)
    {
        removePrefixedAttributes(firstChild, re);
    }
}

function cleanUp(doc)
{
    var elem = doc.documentElement;
    var re = getNSRegExp(elem);

    removePrefixedAttributes(doc.documentElement, re);

    return doc;
}

export default function(url)
{
    if (!cando.ajax || !cando.parseXML)
    {
        return Promise.reject(new Error("Browser does not support SVG or XML parsing"));
    }

    return ajax({
        url: url,
        dataType: "text"
    }).then(function (xml)
    {
        //xml = xml.replace(/(xmlns:sodipodi|xmlns:inkscape|inkscape:[^=]+|sodipodi:[^=]+)="[^"]*"\s*/g,"");

        return cleanUp(parseXML(xml));
    });
};

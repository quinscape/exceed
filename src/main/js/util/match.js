"use strict";

var match = {
    /**
     * Cross-browser matchesSelector method that checks all common browser specific variants and then does a
     * querySelectorAll fallback.
     *
     * @param element       {Element}
     * @param selector      {string} selector string
     * @returns {boolean} true if the element matches the selector
     */
    matchesSelector: (function ()
    {
        var proto = window.Element.prototype;
        var matchesSelector = (
            proto.matches ||
            proto.matchesSelector ||
            proto.webkitMatchesSelector ||
            proto.mozMatchesSelector ||
            proto.msMatchesSelector ||
            proto.oMatchesSelector
        );

        if (matchesSelector)
        {
            return function (elem, selector)
            {
                if (elem.nodeType !== Node.ELEMENT_NODE)
                {
                    return false;
                }
                return matchesSelector.call(elem, selector);
            };
        }
        else
        {
            console.warn("matchesSelector: Using slow querySelectAll matching");
            return function (element, selector)
            {
                if (elem.nodeType !== Node.ELEMENT_NODE)
                {
                    return false;
                }
                // slow fallback via querySelectorAll
                var matches = (element.document || element.ownerDocument).querySelectorAll(selector);

                for (var i = 0; i < matches.length; i++)
                {
                    var match = matches[i];
                    if (match === element)
                    {
                        return true;
                    }
                }

                return false;
            };
        }
    })(),
    /**
     * Returns the first parent of the given element that matches the given selector or null if no such parents
     * exist.
     *
     * @param element       {Element}
     * @param selector      {string} selector string
     * @returns {Element}   first parent to match the selector or null if none match it.
     */
    closest: function (element, selector)
    {
        while ((element = element.parentNode))
        {
            if (match.matchesSelector(element, selector))
            {
                return element;
            }
        }
        return null;
    }
};

module.exports = match;

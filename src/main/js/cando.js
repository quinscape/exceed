var createXMLHTTPObject = require("./util/xhr-factory");
var xmlParser = require("./util/parseXML");

/**
 * Runs feature detections and stores the result for future use.
 *
 * @type {{pushState: boolean, ajax: boolean, localStorage: boolean, sessionStorage: boolean, svg: boolean, parseXML: boolean }}
 */
module.exports = {
    /**
     * Browser supports history.pushState()
     */
    pushState: typeof window !== "undefined" && "pushState" in window.history && typeof window.history["pushState"] === "function",
    /**
     * Browser supports AJAX
     */
    ajax: !!createXMLHTTPObject(),

    localStorage: (function ()
    {
        try
        {
            return typeof window !== "undefined" && "localStorage" in window && window["localStorage"] !== null;
        }
        catch (e)
        {
            return false;
        }
    })(),

    sessionStorage: (function ()
    {
        try
        {
            return typeof window !== "undefined" &&"sessionStorage" in window && window["sessionStorage"] !== null;
        }
        catch (e)
        {
            return false;
        }
    })(),

    svg: (function ()
    {
        try
        {
            return typeof document !== "undefined" && !!document.createElementNS && !!document.createElementNS('http://www.w3.org/2000/svg', "svg").createSVGRect;
        }
        catch (e)
        {
            return false;
        }
    })(),

    parseXML: !!xmlParser
};

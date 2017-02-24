const assign = require("object-assign");
const createXMLHTTPObject = require("./util/xhr-factory");
const xmlParser = require("./util/parseXML");

/**
 * Runs feature detections and stores the result for future use.
 *
 * @type {{pushState: boolean, ajax: boolean, localStorage: boolean, sessionStorage: boolean, svg: boolean, parseXML: boolean }}
 */

var Features = {};

function run()
{

    assign(Features, {
        /**
         * Browser supports history.pushState()
         */
        pushState: typeof window !== "undefined" && window.history && "pushState" in window.history && typeof window.history["pushState"] === "function",
        /**
         * Browser supports AJAX
         */
        ajax: !!createXMLHTTPObject(),

        localStorage: (function ()
        {
            try
            {
                return typeof window !== "undefined" && "localStorage" in window && !!window["localStorage"];
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
                return typeof window !== "undefined" && "sessionStorage" in window && !!window["sessionStorage"];
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

        webSockets: (function ()
        {
            try
            {
                return typeof window !== "undefined" && "WebSocket" in window && !!window["WebSocket"];
            }
            catch (e)
            {
                return false;
            }
        })(),

        parseXML: !!xmlParser
    });
}

run();
/**
 * Should only be used to redo testing with a new mocked environment.
 *
 * @type {function}
 */
Features.rerunFeatureTests = run;

module.exports = Features;




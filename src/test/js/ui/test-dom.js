/**
 * Test helper module for js dom integration. Must be required before react.
 *
 */
import { JSDOM } from "jsdom";

var testDom = {
    setup: function ()
    {
        // must happen before react inclusion
        const dom = new JSDOM('<!doctype html><html><body><div class="root"></div></body></html>');
        global.document = dom.window.document;
        global.window = document.defaultView;
        global.navigator = {userAgent: 'node.js', "platform" : "node.js", appName: "mocha test"};

        return testDom;
    },
    /**
     * Unmounts the current component mounted in our jsdom container element
     */
    cleanup: function ()
    {
        require("react-dom").unmountComponentAtNode( global.document.body.firstChild);
    },
    /**
     * Renders the given component into the global jsdom document and calls the given callback when done.
     */
    render: function(component, callback)
    {
        require("react-dom").render(component, global.document.body.firstChild, callback);
    }
};
module.exports = testDom;


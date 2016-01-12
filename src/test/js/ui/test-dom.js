/**
 * Test helper module for js dom integration. Must be required before react.
 *
 */
var jsdom = require('jsdom');

// must happen before react inclusion
global.document = jsdom.jsdom('<!doctype html><html><body><div class="root"></div></body></html>');
global.window = document.defaultView;
global.navigator = {userAgent: 'node.js'};

module.exports = {
    /**
     * Unmounts the current component mounted in our jsdom container element
     */
    cleanup: function ()
    {
        require("react-dom").unmountComponentAtNode(document.body.firstChild);
    },
    /**
     * Renders the given component into the global jsdom document and calls the given callback when done.
     */
    render: function(component, callback)
    {
        require("react-dom").render(component, document.body.firstChild, callback);
    }
};


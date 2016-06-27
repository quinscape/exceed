var React = require("react");

/**
 * ViewContext allows the definition of view context scoped values inside a view.
 */
var ViewContext = React.createClass({
    shouldComponentUpdate: function ()
    {
        return false;
    },
    render: function ()
    {
        return (<span/>);
    }
});

module.exports = ViewContext;

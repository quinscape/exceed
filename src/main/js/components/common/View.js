var React = require("react");

var View = React.createClass({
    render: function ()
    {
        return (
            <div>
                { this.props.children }
            </div>
        );
    }
});

module.exports = View;

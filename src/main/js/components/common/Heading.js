
var React = require("react");

var Heading = React.createClass({

    render: function ()
    {
        return (
            <h2>
                { this.props.value }
            </h2>
        )
    }
});

module.exports = Heading;

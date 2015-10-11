
var React = require("react/addons");

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

var HR = React.createClass({

    render: function ()
    {
        return (
            <hr/>
        )
    }
});

Heading.HR = HR;

module.exports = Heading;

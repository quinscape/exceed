
var React = require("react/addons");

var Heading = React.createClass({

    render: function ()
    {
        return (
            <h1>
                { this.props.value }
            </h1>
        )
    }
});
module.exports = Heading;

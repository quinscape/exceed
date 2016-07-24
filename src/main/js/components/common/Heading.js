
var React = require("react");

var Heading = React.createClass({

    render: function ()
    {
        var icon = this.props.icon

        return (
            <h2>
                { icon && <span className={ "text-info glyphicon glyphicon-" + icon }/> }
                { this.props.value }
            </h2>
        )
    }
});

module.exports = Heading;

var React = require("react");

var FormElement = require("./FormElement");

var StaticText = FormElement(
    React.createClass({
        render: function ()
        {
            return (
                <p id={ this.props.id } className="form-control-static">
                    { this.props.valueLink.value }
                </p>
            );
        }
    }
));

module.exports = StaticText;

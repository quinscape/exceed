var React = require("react");

var FormGroup = require("./FormElement");

var FormBlock = React.createClass({



    render: function ()
    {
        return (
            <div className="form-block">
                { this.props.children }
            </div>
        );
    }
});

module.exports = FormBlock;

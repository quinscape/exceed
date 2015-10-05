var React = require("react/addons");

var Alert = React.createClass({
    propTypes: React.PropTypes.string.isRequired,
    render: function ()
    {
        return (
            <div className="bg-danger">
                { this.props.message }
            </div>
        );
    }
});

module.exports = Alert;

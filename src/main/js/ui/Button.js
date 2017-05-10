const React = require("react");

const ActionComponent = require("./ActionComponent");

var Button = React.createClass({

    propTypes: {
        text: React.PropTypes.string,
        icon: React.PropTypes.string,
        onClick: React.PropTypes.func
    },

    render: function ()
    {
        return (
            <ActionComponent {... this.props} defaultClass="btn-default" />
        );
    }
});
module.exports = Button;

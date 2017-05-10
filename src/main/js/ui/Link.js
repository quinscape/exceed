const React = require("react");

const ActionComponent = require("./ActionComponent");

var Link = React.createClass({

    propTypes: {
        text: React.PropTypes.string,
        icon: React.PropTypes.string,
        href: React.PropTypes.string,
        onClick: React.PropTypes.func,
        progressive: React.PropTypes.bool
    },

    render: function ()
    {
        return (
            <ActionComponent {... this.props} defaultClass="btn-link" />
        );
    }
});
module.exports = Link;

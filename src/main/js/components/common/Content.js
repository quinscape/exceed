const React = require("react");

var Content = React.createClass({

    render: function ()
    {
        return (
            <div className="view-content">
                { this.props.children }
            </div>
        );
    }
});

module.exports = Content;

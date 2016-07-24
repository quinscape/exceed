var React = require("react");

var View = React.createClass({

    componentDidMount: function ()
    {
        document.title = this.props.title;
    },

    componentDidUpdate: function ()
    {
        document.title = this.props.title;
    },

    render: function ()
    {
        return (
            <div>
                { this.props.children }
            </div>
        );
    }
});

module.exports = View;

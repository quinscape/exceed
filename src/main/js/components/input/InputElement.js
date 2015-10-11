
var React = require("react/addons");

var InputElement = React.createClass({

    onChange: function ()
    {
        console.log("change");
    },

    render: function ()
    {
        return (
            <form action="/app/exceed/">
                <input type="text" name="test" defaultValue="Change me" onChange={ this.onChange } />
            </form>
        )
    }
});

module.exports = InputElement;

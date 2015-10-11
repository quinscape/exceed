
var React = require("react/addons");

var Counter = React.createClass({

    getInitialState: function ()
    {
        return {
            count: this.props.value
        }
    },

    onChange: function ()
    {
        this.setState({
            count: this.state.count + 1
        });
    },

    render: function ()
    {
        return (
            <div className="counter">
                <h3>{ this.state.count }</h3>
                <input type="submit" className="btn btn-primary" value="aaa" onClick={ this.onChange } />
            </div>
        );
    }
});

module.exports = Counter;

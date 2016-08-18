
var React = require("react");

var Counter = React.createClass({

    getInitialState: function ()
    {
        return {
            count: this.props.value || 0
        }
    },

    render: function ()
    {
        return (
            <div className="counter">
                <h3>{ this.state.count }</h3>
                <input type="submit" className="btn btn-primary" value="++" onClick={ () => {
                        this.setState({
                            count: this.state.count + 1
                        });
                } } />
            </div>
        );
    }
});

module.exports = Counter;

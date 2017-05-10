const React = require("react");
const Event  = require("../util/event");

function calculateHeight(customFn)
{
    if (customFn)
    {
        return customFn();
    }
    else
    {
        return window ? window.innerHeight : 500;
    }
}

/**
 * High level component to provide automatic vertical scaling to components. It calculates the height based on
 * the current available window height and updates on window resize.
 *
 * @param Component
 * @returns {*}
 */
module.exports = function (Component)
{
    return React.createClass({

        getInitialState: function ()
        {
            this.timerId = null;

            return {
                height: calculateHeight(Component.calculateHeight)
            };
        },

        componentDidMount: function ()
        {
            window && Event.add(window, "resize", this.onResize, false);
        },

        componentWillUnmount: function ()
        {
            window && Event.remove(window, "resize", this.onResize, false);
        },

        onResize: function ()
        {
            if (this.timerId)
            {
                window.clearTimeout(this.timerId);
            }


            this.timerId = window.setTimeout(() =>
                {
                    this.timerId = null;

                    this.setState({
                        height: calculateHeight(Component.calculateHeight)
                    });
                },
                100
            );
            //console.log("RESIZE");
        },


        render: function ()
        {
            return (
                <Component
                    height={ this.state.height }
                    {... this.props}
                />
            );
        }
    });
};


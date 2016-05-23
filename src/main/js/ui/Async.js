var React = require("react");
var ErrorReport = require("./ErrorReport.es5");
/**
 * Async is a high level component that wraps another component to provide it with asynchronously fetched data.
 *
 * The wrapped component must define a static "fetch" method that returns a Promise. The resolved value of the promise willl be merged
 * with the props defined from the outside.
 *
 * @param Component     Component to wrap
 * @returns {function} Async HLC
 */
function Async(Component)
{
    return React.createClass({

        getInitialState: function ()
        {
            return {
                data: null,
                rejected: false
            }
        },

        componentDidMount: function ()
        {
            Component.fetch().then((data) =>
                {
                    this.setState({
                        data: data
                    });
                })
                .catch((err) =>
                {
                    this.setState({
                        data: err,
                        rejected: true
                    });
                });
        },
        render: function ()
        {
            var asyncData = this.state.data;

            if (!asyncData)
            {
                return <p>Loading...</p>;
            }
            else
            {
                if (this.state.rejected)
                {
                    return (
                        <ErrorReport text={ "Error fetching data for " + Component.displayName } error={ asyncData } />
                    )
                }
                return (
                    <Component {... this.props} data={ asyncData }/>
                );
            }
        }
    });
}

module.exports = Async;

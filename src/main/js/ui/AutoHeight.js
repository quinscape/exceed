import Event from "../util/event";
import React from "react";

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
export default function (Component)
{
    return (class AutoHeight extends React.Component {

        timerId = null;

        state = {
            height: calculateHeight(Component.calculateHeight)
        };

        componentDidMount()
        {
            window && Event.add(window, "resize", this.onResize, false);
        }

        componentWillUnmount()
        {
            window && Event.remove(window, "resize", this.onResize, false);
        }

        onResize = ev =>
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
        };

        render()
        {
            return (
                <Component
                    height={ this.state.height }
                    {... this.props}
                />
            );
        }
    });
}


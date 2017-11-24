import React from "react"

import { isHydrationRender } from "../reducers/meta"

import store from "../service/store"

/**
 * A high-order component for components that need to render a different markup for server-rendering and initial server-
 * rendering hydration.
 *
 * @param Component     component to enhance
 * @param [PlaceHolder] option placeholder component. If not given, nothing is rendered.
 * @returns {{}}
 */
export default function(Component, PlaceHolder)
{
    return class extends React.Component
    {
        state = {
            initial : true
        };

        componentDidMount()
        {
            const state = store.getState();
            if (isHydrationRender(state))
            {
                this.setState({
                    initial: false
                });
            }
        }

        render()
        {
            const { initial } = this.state;
            const isHydration = isHydrationRender(store.getState());

            //console.log({initial, isHydration});

            if (initial && isHydration)
            {
                return (
                    !!PlaceHolder ?
                    <PlaceHolder
                        {... this.props}
                    /> : <span/>
                );
            }

            return (
                <Component
                    {... this.props}
                />
            )
        }
    }
}

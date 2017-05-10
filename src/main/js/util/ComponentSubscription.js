import React from "react"

import { storeShape } from "react-redux/lib/utils/PropTypes"

function stateIdentity(oldState, newState)
{
    return oldState === newState;
}

/**
 * High-order component subscribing to the redux store, filtering updates by using the given comparator
 * function.
 *
 * @param Component     base component
 * @param comparator    {Function?} compares two state (slices) for equality
 * @returns {ComponentSubscription} high-order-component
 * @constructor
 */
export default function ComponentSubscription(Component, comparator = stateIdentity)
{
    return class ComponentSubscription extends React.Component
    {
        static displayName = Component.displayName || "ComponentSubscription";

        static contextTypes =  {
            store: storeShape
        };

        componentDidMount()
        {

            const store = this.context.store;

            let oldState = store.getState();

            this._subscription = store.subscribe(() => {
                const newState = store.getState();

                if (!comparator(oldState, newState))
                {
                    oldState = newState;
                    this.forceUpdate();
                }
                // else
                // {
                //     if (__DEV)
                //     {
                //         console.debug(Component.displayName + " ignores update: ");
                //     }
                // }
            });
        }

        componentWillUnmount()
        {
            this._subscription();
        }

        render()
        {
            const store = this.props.store || this.context.store;
            
            return (
                <Component {... this.props} store={ store }/>
            );
        }
    };
}

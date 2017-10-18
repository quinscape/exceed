import appUri from "../util/app-uri";
import cast from "../util/cast";
import when from "../util/when";

import DataCursor from "../domain/cursor"
import DataGraph from "../domain/graph"
import { navigateView } from "../actions/view"
import { updateScope } from "../actions/scope"
import { getDomainData, getLocation, getScopeGraph, getViewModel, getViewStateTransition, getAuthentication, getStateMachine } from "../reducers"

import uuid from "uuid";

class RTView {
    /**
     * Runtime view API. Gets created in the automatically rendered code for views and assigned to the variable "_v".
     * The server-side expression transformation will generate references to this API.
     *
     * We run this in two different modes:
     *
     *  * With a store reference in the constructor, we will always use that store's getState() to receive the current state.
     *
     *  * Without store, we work on a state object we receive in the constructor. This is used to create a form-state for
     *    a future redux state within app state hydration.
     *
     * @param state     {?object} redux state
     * @param store     {?Store} redux store
     * @constructor
     */
    constructor(state, store)
    {
        this.store = store;

        if (state)
        {
            this.updateState(state);
        }
        this.uri = require("../util/uri");
        this.cast = cast;
        this.when = when;
    }

    getState()
    {
        const { store, state } = this;

        if (!store)
        {
            return state;
        }
        else
        {
            return store.getState();
        }
    }

    getViewModel()
    {
        const { store } = this;

        if (!store)
        {
            return this.viewModel;
        }
        else
        {
            return getViewModel(store.getState());
        }
    }

    updateState(state)
    {

        this.state = state;
        this.viewModel = getViewModel(state);
    }

    /**
     * Injects data into the static props of a component if the static props of a component
     * do not contain a value for that property.
     *
     * This way we can always override the data query / injection mechanisms in favor of local data.
     *
     * @param props     static props
     * @param data      data injection for this prop as per component query definition
     * @returns {*}
     */
    inject(props, data)
    {
        for (let k in data)
        {
            if (data.hasOwnProperty(k) && !props.hasOwnProperty(k))
            {
                props[k] = data[k];
            }
        }
        return props;
    }

    param(name)
    {
        /** @see LocationParamsProvider.java */
        return getLocation(this.getState()).params[name];
    }

    /**
     * Navigates the current view state to the given location
     *
     * @param location      location, potentially with path variables (e.g. "/path/{myVar}")
     * @param params        params for the location
     *
     * @returns {Promise}   promise resolving when the navigation change has ended, i.e. the view rendering of the new view ended.
     */
    navigateTo(location, params)
    {
        return this.store.dispatch(
            navigateView({
                url: appUri( location, params)
            })
        );
    }

    transition(name)
    {
        return getViewStateTransition(this.getState(), name);
    }

// Import scope functions into runtime view prototype chain
    scope(name)
    {
        const cursor = this.scopeCursor(name);
        return cursor.get();
    }

    scopeCursor(name)
    {
        const state = this.getState();

        //console.log("scopeCursor", state);

        const scope = DataGraph(
            getScopeGraph(state)
        );
        return new DataCursor(getDomainData(state), scope, typeof name === "string" ? [ name ] : name );
    }

    updateScope(name, value)
    {
        this.store.dispatch(
            updateScope(
                name,
                value
            )
        )
    }

    debug(args)
    {
        console.log("debug()", args);
    }

    isNew(cursor)
    {
        const domainObject = cursor.get();

        //console.log({domainObject, cursor});

        return domainObject && domainObject.id === null;
    }

    model(chain)
    {

        if (!chain || chain.length < 1)
        {
            throw new Error("Invalid model chain: " + chain);
        }

        let component = this.getViewModel().content[chain[0]];

        for (let i = 1; i < chain.length; i++)
        {
            component = component.kids[chain[i]];
        }
        return component;
    }

    cursorExpr(component, name, context)
    {

        //console.log("cursorExpr", {component, name, context});

        return component.exprs[name].fn(context, this);
    }

    conditional(cond)
    {
        if (cond)
        {
            return Promise.resolve(true);
        }
        else
        {
            return Promise.reject(false);
        }
    }

    uuid()
    {
        return uuid.v4();
    }

    isAdmin()
    {
        return this.hasRole("ROLE_ADMIN");
    }

    hasRole()
    {
        return getAuthentication(this.getState()).roles.indexOf(role) >= 0;
    }

    fieldId(base)
    {
        return base;
    }

    isValidTransition(stateMachineName, src, dst)
    {
        const stateMachine = getStateMachine(this.getState(), stateMachineName);
        if (!stateMachine)
        {
            throw new Error("Invalid state machine name '" + stateMachine + "'");
        }

        const srcState = getState(stateMachine, src);
        getState(stateMachine, dst);

        return srcState.indexOf(dst) >= 0;
    }
}

function getState(stateMachine, name)
{
    const state = stateMachine.states[name];
    if (!state)
    {
        throw new Error("State machine '" + stateMachine.name + "' has no state '" + name + "'");
    }
    return state;
}

export default RTView

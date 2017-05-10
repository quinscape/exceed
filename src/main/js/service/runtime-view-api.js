import appUri from "../util/app-uri";
import Scope from "./scope"

import store from "../service/store"
import { navigateView } from "../actions/view"
import { getViewModel, getViewState, getLocation } from "../reducers"

/**
 * Runtime view API. Gets created in the automatically rendered code for views and assigned to the variable "_v".
 * The server side expression transformation will generate references to this API.
 *
 * @param model     view model
 * @param data      view data
 * @constructor
 */
function RTView(store)
{

    const viewModel = getViewModel(store.getState());

    this.store = store;
    this.name = viewModel.name;
    this.content = viewModel.content;
    //this.data = data;
    this.viewModel = viewModel;
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
RTView.prototype.inject = function (props, data)
{
    for (var k in data)
    {
        if (data.hasOwnProperty(k) && !props.hasOwnProperty(k))
        {
            props[k] = data[k];
        }
    }
    return props;
};

RTView.prototype.param = function (name)
{
    const state = store.getState();
    /** @see LocationParamsProvider.java */
    return getLocation(state).params[name];
};


/**
 * Navigates the current view state to the given location
 *
 * @param location      location, potentially with path variables (e.g. "/path/{myVar}")
 * @param params        params for the location
 *
 * @returns {Promise}   promise resolving when the navigation change has ended, i.e. the view rendering of the new view ended.
 */
RTView.prototype.navigateTo = function (location, params)
{
    return store.dispatch(
        navigateView({
            url: appUri( location, params)
        })
    );
};

RTView.prototype.uri = require("../util/uri");

RTView.prototype.transition = function (name)
{
    const state = store.getState();
    const viewState = getViewState(state);
    if (!viewState)
    {
        throw new Error("transition but no view state");
    }

    var transition = viewState.transitions[name];
    if (!transition)
    {
        throw new Error("Transition '" + name + "' not found");
    }
    return transition;
};

// Import scope functions into runtime view prototype chain
RTView.prototype.scope = Scope.property;

RTView.prototype.scopeCursor = Scope.propertyCursor;

module.exports = RTView;

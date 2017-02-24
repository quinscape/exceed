var appUri = require("../util/app-uri");

var Scope = require("./scope");

var viewService;

function getViewService()
{
    if (!viewService)
    {
        viewService = require("../service/view");
    }
    return viewService;
}

/**
 * Runtime view API. Gets created in the automatically rendered code for views and assigned to the variable "_v".
 * The server side expression transformation will generate references to this API.
 *
 * @param model     view model
 * @param data      view data
 * @constructor
 */
function RTView(model, data)
{
    this.name = model.name;
    this.root = model.root;
    this.data = data;
    this.viewModel = model;
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
    /** @see LocationParamsProvider.java */
    return this.data._exceed.location.params[name];
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
    return getViewService().navigateTo({
        url: appUri( location, params)
    });

};

RTView.prototype.uri = require("../util/uri");

RTView.prototype.transition = function (name)
{
    var viewState = getViewService().getRuntimeInfo().viewState;
    if (!viewState)
    {
        throw new Error("transitionIsDiscard but no view state");
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

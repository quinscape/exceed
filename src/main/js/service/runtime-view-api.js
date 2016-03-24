var Promise = require("es6-promise-polyfill").Promise;

var actionService = require("../service/action");
var uri = require("../util/uri");
var sys = require("../sys");

var viewService;

var Throbber = require("../ui/throbber");


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
    return this.data._exceed.locationParams[name];
};

/**
 * Returns a promise for the given action execution if the optional name is set, it will be copied to
 * identifying "action" property, otherwise the model must already contain the "action" property.
 *
 * @param model     {object} JSON action model
 * @param name      {?name} optional name parameter.
 * @returns {Promise}
 */
RTView.prototype.action = function (model, name)
{
    // (name, model)?
    if (name !== undefined)
    {
        // yes -> make sure the given object has the right action attribute
        model.action = name;
    }

    if (!model.action)
    {
        return Promise.reject(new Error("No action property set"));
    }

    return actionService.execute(model);
};

function defaultError(err)
{
    console.error(err);
    return Promise.reject(err);
}

function throbberError(e)
{
    Throbber.disable();
    return defaultError(e);
}

/**
 * Default action error handling.
 *
 * @param promise   promise to observe
 */
RTView.prototype.observe = function (promise)
{
    var enableThrobber = require("../cando").ajax;
    enableThrobber && Throbber.enable();

    return ( enableThrobber ?
        promise.then(Throbber.disable, throbberError) :
        promise.catch(defaultError)
    );
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
    return getViewService().navigateTo( uri( "/app/" + sys.appName + location, params));

};

RTView.prototype.update = function (id, vars)
{
    getViewService().updateComponent(id, vars || {});
};

RTView.prototype.uri = require("../util/uri");

module.exports = RTView;

"use strict";

var React = require("react");
var extend = require("extend");
var ajax = require("./service/ajax");

var uri = require("./util/uri");

var idCount = 0;

if (process.env.NODE_ENV !== "production")
{
    var warnOnce = require("./util/warn-once");
}

var Alert = React.createClass({
    render: function ()
    {
        var detail;
        if (this.props.detail)
        {
            detail = (
                <pre className="text-danger">
                    { this.props.detail }
                </pre>
            )
        }

        return (
            <div className="alert alert-danger">
                { this.props.message }
                { detail }
            </div>
        );
    }
});
var missingFunctionValues = {};

var compositors = [];

/**
 *
 * Creates a compositor instance tied to a mount point element in the DOM.
 *
 * @param views     static require map a la bulkify
 * @param elem      DOM element
 * @constructor
 */
function Compositor(views, elem)
{

    this.views = views;
    this.elem = elem;

    if (elem.id)
    {
        this.id = elem.id;
    }
    else
    {
        this.id = "id" + (++idCount);
        elem.id = this.id;
    }

    compositors.push(this);
}

extend(Compositor.prototype, {

    /**
     * Creates a view compontent
     *
     * @param viewName      {string|ReactClass} view module name or component class
     * @param props         view component props
     *
     * @returns {ReactElement} view component element
     */
    createView: function (viewName, props)
    {
        if (!viewName)
        {
            return (
                <Alert message={ "No viewName" } detail={ "Props:\n" + JSON.stringify(props, null, "  ") }/>
            );
        }

        if (typeof viewName === "string")
        {
        var parts = viewName.split("/");

        var pointer = this.views;
        for (var i = 0; i < parts.length; i++)
        {
            pointer = pointer[parts[i]];
            if (!pointer)
            {
                return (<Alert message={ "React view module '" + viewName + "' not found." }
                               detail={ "Props:\n" + JSON.stringify(props, null, "  ") }/> );
            }
        }
            return React.createElement(pointer, props);
        }
        else
        {
            // non-string viewName assumed to the react component
            return React.createElement(viewName, props);
        }
    },

    /**
     * Renders a view or a component into the given DOM element.
     *
     * @param viewName              {(string|Object)?}view module name or react component
     * @param props                 {Object?} component props. If not given, an initial data lookup is performed
     * @param callback              {function?} callback to call after rendering (gets passed to React.render)
     */
    render: function (viewName, props, callback)
    {

        if (!props)
        {
            var script = document.getElementById(this.id + "-data");

            if (script && script.getAttribute("type") === "application/x-initial-data")
            {
                var data = JSON.parse(script.innerHTML);
                props = data.props;
            }
            else
            {
                props = null;
            }
        }

        var compositor = this;

        viewName = viewName || this.elem.dataset.compositor;

        ri.startFunctionTracking(this.id);

        var component = compositor.createView(viewName, props);

        React.render(
            component,
            compositor.elem,
            function ()
            {
                compositor.rootComponent = component;
                compositor.viewName = viewName;
                compositor.props = props;

                var missingCalls = ri.getMissingCalls(compositor.id);
                if ( missingCalls.length && Compositor.retryMissingFunctions )
                {
                    compositor.requestMissing(missingCalls).then(function (data)
                    {
                        ri.fillCache(data);

                        ri.startFunctionTracking(compositor.id);

                        React.render(
                            component,
                            compositor.elem,
                            function ()
                            {
                                var missingFunctionValues = ri.getMissingCalls(compositor.id);
                                if (missingFunctionValues.length)
                                {
                                    console.warn("Still missing function values after requesting them: " + JSON.stringify(missingFunctionValues))
                                }

                                if (callback)
                                {
                                    callback(compositor);
                                }
                            }
                        )
                    })
                    .catch(function (e)
                    {
                        console.error("Error rerendering", e);
                    });
                }
                else
                {
                    if (callback)
                    {
                        callback(compositor);
                    }
                }
            }
        );
    },
    updateComponent: function (component, vars)
    {
        var props = component.props;
        if (!props || !props._injection)
        {
            throw new Error(component + " is not a injected component");
        }

        var compositor = this;
        var index = props._injection.index;
        ajax({
            url: uri("/inject/update", {
                compositor: compositor.viewName,
                index: index,
                vars: JSON.stringify(vars)
            }),
            method: "GET"
        }).then(function (data)
        {
            var oldProps = compositor.rootComponent.props;
            var newProps = React.addons.update(oldProps, {
                _injections: {$splice: [[index, 1, data.value]]},
                _functionValues: {$set: data.functionValues}
            });

            compositor.render(compositor.viewName, newProps, Compositor.updateHistory);
        })
        .catch(function (err)
        {
            console.log("Error updating component #" + index + " in '" + compositor.viewName + ", vars = " + JSON.stringify(vars) + ":\n", err);
        });
    },

    requestMissing: function(missingFunctionValues)
    {
        var json = JSON.stringify({
            calls: missingFunctionValues
        });

        console.info("Requesting missing function values: " + json);

        return ajax({
            url: uri("/inject/runtimeCalls", {
                compositor: this.viewName,
                calls: json
            })
        });
    },

    navigateTo: function(url)
    {
        var compositor = this;

        return ajax({
            url: url
        }).then(function (data)
        {
            window.history.pushState({
                jsViewName: data.jsViewName,
                props: data.props,
                id: compositor.elem.id
            }, "title", url);

            return new Promise(function (resolve, reject)
            {
                compositor.render(data.jsViewName, data.props, resolve);
            });
        })
        .catch(function(e)
        {
            // if we received a NOT_ACCEPTABLE or there was a JSON parsing error
            if (e.status === 406 || (e.error && e.error.indexOf("Error parsing JSON") >= 0))
            {
                // warn and redirect to the original URL

                if (process.env.NODE_ENV !== "production")
                {
                    warnOnce(url, "AJAX request to '" + url + "' failed.\nYou might want to declare the link ajax={ false }");
                }

                window.location.href = url;
            }
        });
    }
});

Compositor.clearCompositorRegistry = function()
{
    compositors = [];
};

Compositor.find =  function(elem)
{
    for (var i = 0; i < compositors.length; i++)
    {
        var compositor = compositors[i];
        if (compositor === elem || compositor.elem.contains(elem))
        {
            return compositor;
        }
    }
    return null;
};

Compositor.updateHistory = function (compositor)
{
    if (compositor.id === "root")
    {
        var state = {
            jsViewName: compositor.viewName,
            props: compositor.props,
            id: compositor.id
        };

        //console.log("UPDATE", state.props._injections);

        window.history.replaceState(state, "title", window.location.href);
    }
};

window.onpopstate = function (ev)
{
    var state = ev.state;
    //console.log("POP", state.jsViewName, history.state.jsViewName);

    var elem = document.getElementById(state.id);
    var mp = Compositor.find(elem);

    mp.render(state.jsViewName, state.props);

};

// if we encounter missing functions during a render, we request the missing values via AJAX and rerender
Compositor.retryMissingFunctions = true;

module.exports = Compositor;

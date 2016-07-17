var currentViewData = typeof window !== "undefined" && window._exceed_initial_data ? window._exceed_initial_data : {
    _exceed : {
        translations: {

        }
    }
};

var viewService = {

    _init: function (data)
    {
        currentViewData = data;
    },

    /**
     * The runtime info data map contains the information the registered
     * de.quinscape.exceed.runtime.service.RuntimeInfoProvider implementations provided.
     * @returns {Object} map mapping declared info names to the current result.
     */
    getRuntimeInfo: function()
    {
        return currentViewData['_exceed'];
    }
};

module.exports = viewService;

const React = require("react");
const ReactDOM = require("react-dom");
const assign = require("object-assign");
const Promise = require("es6-promise-polyfill").Promise;
const security = require("./security");
const uri = require("../util/uri");
const ajax = require("./ajax");
const componentService = require("./component");

const DataGraph = require("../util/data-graph");

const modelUtil = require("../util/model");

const update = require("react-addons-update");

const FormContext = require("../util/form-context");

const LinkedStateMixin = require("react-addons-linked-state-mixin");

const keys = require("../util/keys");

var InPageEditor = false;
//if (process.env.NODE_ENV !== "production")
//{
//    InPageEditor = require("../editor/InPageEditor");
//}

const ThrobberComponent = require("../ui/throbber").ThrobberComponent;

const ErrorReport = require("../ui/ErrorReport.es5");

const viewRenderer = require("./view-renderer");

var currentViewModel = {
    root: {
        name: "[String]",
        attrs: {
            value: "No View"
        }
    }
};


var scopeDataGraph;
var scopeDirty = {};

function onChangeScope(newGraph, path)
{
    if (path == null)
    {
        // assume global update with fresh context data
        scopeDirty = {};
    }
    else
    {
        var name = path[0];
        if (!viewService.getRuntimeInfo().scopeInfo.viewContext[name])
        {
//            console.log("MARK DIRTY ", name);
            scopeDirty[name] = true;
        }
    }

    //console.log("NEW SCOPE GRAPH", newGraph);

    scopeDataGraph = newGraph;

    //console.log("VD", currentViewData);

    currentViewData = update(currentViewData, {
        _exceed: { scope : {$set : newGraph.rootObject}}
    });
    //console.log("VD NEW", currentViewData);

    render(
        <ViewComponent
            model={ currentViewModel }
            componentData={ currentViewData }
            components={ componentService.getComponents() }
        />
    ).catch(function (err)
    {
        console.error(err);
    });
}

function updateViewData(newViewData)
{
//    console.log("UPDATE VIEW-DATA", newViewData);

    currentViewData = newViewData;

    var scopeData = viewService.getRuntimeInfo().scope;
    if (scopeData)
    {
        scopeDataGraph = new DataGraph(require("./domain").getDomainTypes(), scopeData, onChangeScope);
    }
}

// maps logical view names to view components. Used as a cache to make sure we only generate every view once per version
// we encounter.
var renderFnCache = {};

function lookupRenderFn(viewModel, components)
{
    var viewName = viewModel.name;

    var entry = renderFnCache[viewName];

    // if we have no view component or if the view component version is not the same as the view model version
    if (!entry || entry.version !== viewModel.version)
    {
        // we (re)create the view component
        entry = viewRenderer.createRenderFunction(viewModel, components);
        entry.version = viewModel.version;
        renderFnCache[viewName] = entry;
    }

    return entry.fn;
}

function decorateView(viewElem)
{
    return (
        <div>
            { viewElem }
            { security.hasRole("ROLE_EDITOR") && InPageEditor && <InPageEditor model={ currentViewModel } /> }
            <ThrobberComponent/>
        </div>
    )
}
/**
 * Renders the given component with the standard decorations
 *
 * @param viewElem  {ReactElement} view
 * @returns {Promise}   promise that resolves after rendering.
 */
function render(viewElem)
{
    var rootElem = document.getElementById("root");
    if (!rootElem)
    {
        return Promise.reject(new Error("Missing #root DOM element"));
    }

    return new Promise(function (resolve, reject)
    {

        try
        {
            ReactDOM.render(
                decorateView(viewElem),
                rootElem,
                resolve
            );
        }
        catch(e)
        {
            reject(e);
        }
    });
}


var ViewComponent = React.createClass({

    mixins: [ LinkedStateMixin ],

    childContextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    getInitialState: function ()
    {
        return {
            errors: {}
        };

    },

    getChildContext: function()
    {
        return {
            formContext: new FormContext(
                false,
                null,
                null
            )
        };
    },

    render: function ()
    {
        return lookupRenderFn(this.props.model, this.props.components)(this);
    }
});

var fetchDefaultOpts = {
    preview: null
};

function stripExprs(k,v)
{
    if (k !== "exprs")
    {
        return v;
    }
}


assign(viewService, {

    updateComponent: function(id, vars, url)
    {
        if (!id)
        {
            throw new Error("Need id: " + id);
        }

        if (!vars)
        {
            throw new Error("Need vars : " + vars);
        }

        var currentVars = currentViewData[id].vars;

        if (!currentVars)
        {
            throw new Error("No component data for component '" + id +"'");
        }

        vars = update(currentVars, {
            $merge: vars
        });

        var opts = {
            url: uri(window.location.href, {
                _id : id,
                _vars: JSON.stringify(vars)
            }, true),
            headers: {
                "X-ceed-Update": "true"
            }
        };

        if (currentViewModel.preview)
        {
            opts.method =  "POST";
            opts.contentType = "application/json";
            opts.data = JSON.stringify(currentViewModel, stripExprs);
            opts.headers['X-ceed-Preview'] = "true";
        }

        return ajax(opts)
            .then((componentData) =>
            {
                var newViewData = update(currentViewData, {
                    [id] : { $set: componentData }
                });

                if (url)
                {
                    window.history.pushState({
                        viewModel: currentViewModel,
                        viewData: newViewData
                    }, "exceed title", url)
                }

                return this.render(currentViewModel, newViewData, !!url);
            })
            .catch(function (err)
            {
                console.error("Error updating component '" + id + ", vars = " + JSON.stringify(vars), err);
                return Promise.reject(err);
            });
    },

    fetch: function (opts)
    {
        //if (fetchCount++ == 1)
        //{
        //    return Promise.reject();
        //}

        opts = assign({}, fetchDefaultOpts, opts);

        var isPreview = !!opts.preview;
        //console.log("fetchView, isPreview = ", isPreview, JSON.stringify(model, null, "  "));

        var ajaxOpts = {
            url: opts.url || window.location.href
        };

        if (isPreview)
        {
            ajaxOpts.method =  "POST";
            ajaxOpts.contentType = "application/json";
            ajaxOpts.data = opts.preview;
            ajaxOpts.headers = {
                "X-ceed-Preview" : "true"
            }
        }
        else
        {
            ajaxOpts.method =  opts.method;
            ajaxOpts.contentType = "application/json";
            ajaxOpts.data = opts.data || {};
            ajaxOpts.dataType = opts.dataType;
        }

        return ajax(ajaxOpts);
    },

    updateState: function()
    {
        window.history.replaceState({
            viewModel: currentViewModel,
            viewData: currentViewData
        }, "exceed title", window.location.href);
    },

    navigateTo: function(url, data, urlProvider)
    {
        return this.fetch({
                method: data ? "POST" : "GET",
                url: url,
                dataType: "XHR",
                data: data
            })
            .then((xhr) =>
            {
                //console.log("URL", url, xhr.responseURL, xhr.responseText);
                var data = JSON.parse(xhr.responseText);

                if (urlProvider)
                {
                    url = urlProvider(data);
                }
                else
                {
                    url = xhr.responseURL;
                }

                this.updateState();
                window.history.pushState(data, "exceed title", url);
                return this.render(data.viewModel, data.viewData, false);
            })
            .catch(function (err)
            {
                console.log(err);
            });
    },

    /**
     * Renders the given viewModel and viewData
     *
     * @param viewModel     viewModel
     * @param viewData      viewData
     * @param noReplaceState    if true, don't replace the current window state (mostly internal use)
     *
     * @returns {Promise} resolves after rendering is done.
     */
    render: function(viewModel, viewData, noReplaceState)
    {
        if (!viewModel)
        {
            return Promise.reject(new Error("No view model"));
        }

        if (!viewData)
        {
            return Promise.reject(new Error("No view data"));
        }

        currentViewModel = viewModel;
        updateViewData(viewData);

        !noReplaceState && viewService.updateState();

        return render(
            <ViewComponent
                model={ currentViewModel }
                componentData={ currentViewData }
                components={ componentService.getComponents() }
            />
        );
    },

    /**
     * Renders an ErrorReport for the given error into the view.
     *
     * @param err
     * @returns {Promise}
     */
    renderError: function(err)
    {
        currentViewModel.preview = true;

        return render(
            <ErrorReport error={err}/>
        );
    },
    /**
     * Returns the last rendered view model
     * @returns {object} view model
     */
    getViewModel: function ()
    {
        return currentViewModel;
    },
    /**
     * Returns the last used view data
     */
    getViewData: function ()
    {
        return currentViewData;
    },
    /**
     * Returns the current scope data list
     *
     * @returns {DataGraph} scope data graph
     */
    getScope: function ()
    {
        return scopeDataGraph;
    },


    renderFn: function()
    {
        return renderFnCache[currentViewModel.name].src;
    },

    getViewContextKeys: function ()
    {
        return keys(viewService.getRuntimeInfo().scopeInfo.viewContext);
    },

    getDirtyScopeKeys: function ()
    {
        return keys(scopeDirty);
    },

    ViewComponent: ViewComponent,
    scopeDirty : scopeDirty
});

if (typeof window !== "undefined")
{
    window.onpopstate = function (ev)
    {
        var state = ev.state;
//        console.log("POP-STATE", state);
        viewService.render(state.viewModel, state.viewData);
    };

    window.ViewService = viewService;
}


var React = require("react");
var extend = require("extend");
var Promise = require("es6-promise-polyfill").Promise;
var security = require("./security");
var uri = require("../util/uri");
var ajax = require("../service/ajax");

var update = require("react-addons-update");

var InPageEditor = false;
if (process.env.NODE_ENV !== "production")
{
    InPageEditor = require("../editor/InPageEditor");
}

var ThrobberComponent = require("../ui/throbber").ThrobberComponent;

var ErrorReport = require("../ui/ErrorReport.es5");

var viewRenderer = require("./view-renderer");

var currentViewModel = {
    root: {
        name: "[String]",
        attrs: {
            value: "No View"
        }
    }
};

var currentViewData = {};

// maps logical view names to view components. Used as a cache to make sure we only generate every view once per version
// we encounter.
var renderFnCache = {};

function lookupRenderFn(viewModel)
{
    var viewName = viewModel.name;

    var entry = renderFnCache[viewName];

    // if we have no view component or if the view component version is not the same as the view model version
    if (!entry || entry.version !== viewModel.version)
    {
        // we (re)create the view component
        entry = {
            version: viewModel.version,
            fn: viewRenderer.createRenderFunction(viewModel)
        };

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
    render: function ()
    {
        return lookupRenderFn(this.props.model)(this);
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

window.onpopstate = function (ev)
{
    var state = ev.state;
    viewService.render(state);
};

var viewService = {

    updateComponent: function(id, vars)
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
                _xcd_id : id,
                _xcd_vars: JSON.stringify(vars)
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
                return this.render(currentViewModel, update(currentViewData, {
                    [id] : { $set: componentData }
                }));
            })
            .catch(function (err)
            {
                console.error("Error updating component '" + id + ", vars = " + JSON.stringify(vars), err);
                return Promise.reject(err);
            });
    },

    fetch: function (opts)
    {
        opts = extend({}, fetchDefaultOpts, opts);

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
            ajaxOpts.method =  "GET";
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

    navigateTo: function(url)
    {
        return this.fetch({
                url: url
            })
            .then((data) =>
            {
                this.updateState();
                window.history.pushState(data, "exceed title", url);
                return this.render(data, null, false);
            })
            .catch(function (err)
            {
                console.log(err);
            });
    },

    /**
     * Renders the given viewModel and viewData
     *
     * @param viewModel         viewModel
     * @param viewData          viewData
     * @param noReplaceState    if true, don't replace the current window state (mostly internal use)
     *
 * @returns {Promise} resolves after rendering is done.
     */
    render: function(viewModel, viewData, noReplaceState)
    {
        if (viewData)
        {
            currentViewModel = viewModel;
            currentViewData = viewData;
        }
        else
        {
            currentViewModel = viewModel.viewModel;
            currentViewData = viewModel.viewData;
        }

        !noReplaceState && viewService.updateState();

        return render(
            <ViewComponent model={ currentViewModel } componentData={ currentViewData } />
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

    getRuntimeInfo: function()
    {
        return currentViewData['_exceed'];
    },

    ViewComponent: ViewComponent
};
module.exports = viewService;

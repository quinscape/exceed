var ReactDOM = require("react-dom");
var React = require("react");
var Promise = require("es6-promise-polyfill").Promise;
var security = require("./security");

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

module.exports = ViewComponent;

module.exports = {
    /**
     * Renders the given viewModel and viewData
     *
     * @param viewModel     viewModel
     * @param viewData      viewData
     *
     * @returns {Promise} resolves after rendering is done.
     */
    render: function(viewModel, viewData)
    {
        currentViewModel = viewModel;
        currentViewData = viewData;

        return render(
            <ViewComponent model={ currentViewModel } componentData={ currentViewData } />
        );
    },
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

    ViewComponent: ViewComponent
};

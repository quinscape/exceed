var ReactDOM = require("react-dom");
var Promise = require("es6-promise-polyfill").Promise;

var viewAPI = require("./view-api");

var viewComponentRenderer = require("./view-component-renderer");

var currentViewModel = {
    root: {
        name: "[String]",
        attrs: {
            value: "No View"
        }
    }
};

var currentViewData = {};

var viewCache = {};

function lookupComponent(viewModel)
{
    var viewName = viewModel.name;

    var viewComponent = viewCache[viewName];

    if (!viewComponent || viewComponent.version !== viewModel.version)
    {
        viewComponent = viewComponentRenderer.createComponent(viewModel);

        viewCache[viewName] = viewComponent;
    }

    return viewComponent;
}
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

        return new Promise(function (resolve, reject)
        {

            var rootElem = document.getElementById("root");
            if (!rootElem)
            {
                reject(new Error("Missing #root DOM element"));
            }

            var ViewComponent = lookupComponent(viewModel);

            try
            {
                ReactDOM.render(
                    <div>
                        <ViewComponent model={ currentViewModel } componentData={ currentViewData } />
                        { viewAPI.editor( currentViewModel) }
                    </div>,
                    rootElem,
                    resolve
                );
            }
            catch(e)
            {
                reject(e);
            }
        });
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
    }
};

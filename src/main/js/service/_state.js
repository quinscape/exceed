
var util = require("util");
var modelUtil = require("../util/model");
var processService = require("./process");
var viewService = require("./view");

var currentViewModel = {
    name: "_empty",
    root: {
        name: "[String]",
        attrs: {
            value: "No View"
        }
    }
};
var currentViewData = null;
var currentProcessModel = null;
var currentProcessData = null;

function replaceState(url)
{
    if (!url)
    {
        throw new Error("No url");
    }

    var state = ({
        viewModel: currentViewModel,
        viewData: currentViewData,
        processModel: currentProcessModel,
        processData: currentProcessData
    });

//    console.log(util.inspect(state, {showHidden: true, depth: null}));

    window.history.replaceState(state, "exceed title", url);
}

function pushState(url)
{
    if (!url)
    {
        throw new Error("No url");
    }

    window.history.pushState({
        viewModel: currentViewModel,
        viewData: currentViewData,
        processModel: currentProcessModel,
        processData: currentProcessData
    }, "exceed title", url);
}

var stateService = {
    update: function (newModel, newData, url)
    {
        if (!newModel)
        {
            throw new Error("No model");
        }
        if (!newData)
        {
            throw new Error("No data");
        }

        url = url || window.location.href;

        // are we initializing a new process?
        if (modelUtil.isProcess(newModel))
        {
            // yes -> initialize process state and delegate to the process service
            currentProcessModel = newModel;
            currentProcessData  = newData;

            return processService.start(currentProcessModel, currentProcessData);
        }
        else
        {   // no -> model must be a view model

            if (!modelUtil.isView(newModel))
            {
                throw new Error("Model is neither process nor view" + JSON.stringify(newModel));
            }

            // does the view belong to a process?
            var processName = newModel.processName;

            currentViewModel = newModel;
            currentViewData = newData;

            if (processName)
            {
                var viewName = newModel.name;
                if ( viewName.indexOf(processName + "/") !== 0 )
                {
                    throw new Error("View not part of process '" + processName + "' : " + newModel.name )
                }

                if (!currentProcessModel)
                {
                    throw new Error("No process");
                }

                var viewName = viewName.substr(processName.length + 1);
                var state = currentProcessModel.states[viewName];
                if ( !state )
                {
                    throw new Error("No view state '" + viewName + "' in process '" + processName + "'" );
                }

                // are we still in the initial state?
                if (processService.inInitialState(currentProcessData))
                {
                    // yes -> update the current state and replace the history state
                    currentProcessData.currentState = state.name;
                    replaceState(url);
                }
                else
                {
                    // no -> update the current state and push a history state
                    currentProcessData.currentState = state.name;
                    pushState(url);
                }
                return viewService.render(currentViewModel, currentViewData);
            }
            else
            {
                // we're no longer in a process if we were
                currentProcessModel = null;
                currentProcessData = null;
                pushState(url);

                return viewService.render(currentViewModel, currentViewData);
            }
        }
    },


    getProcessData: function ()
    {
        return currentProcessData;
    },


    getProcessModel: function ()
    {
        return currentProcessModel;
    },

    getViewModel: function ()
    {
        return currentViewModel;
    },

    getViewData: function ()
    {
        return currentViewData;
    },

    getRuntimeInfo: function()
    {
        return currentViewData['_exceed'];
    }
};
/**
 * Handles the overall storage of view models, view data and process data over the lifecycle of the application.
 *
 * Tied to and versioned with the browser history.
 *
 * @type {{}}
 */
module.exports = stateService;


window.onpopstate = function (ev)
{
    var state = ev.state;

//    console.log("POP STATE", state);

    currentViewModel = state.viewModel;
    currentViewData = state.viewData;
    currentProcessModel = state.processModel;
    currentProcessData = state.processData;

    viewService.render(currentViewModel, currentViewData);
};


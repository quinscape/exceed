import React from "react"
import { Provider, connect } from 'react-redux'

import { render } from "react-dom"
import { Promise } from "es6-promise-polyfill"
import security from "./security"
import store from "./store"
var componentService = require("./component");

import { setScopeValue } from "../actions/scope"
import { getViewModel, getScopeGraph, getComponentData } from "../reducers"


let InPageEditor = false;
if (process.env.USE_EDITOR)
{
    InPageEditor = require("../editor/InPageEditor");
}

import { ThrobberComponent } from "../ui/throbber"
import Dialog from "../util/dialog"
import ComponentSubscription from "../util/ComponentSubscription"
import viewRenderer from "./view-renderer"
import FormProvider from "../ui/FormProvider"

// maps logical view names to view components. Used as a cache to make sure we only generate every view once per version
// we encounter.
const renderFnCache = {};

/**
 * Preview marker component
 * 
 * @param props
 * @returns {*}
 */
function PreviewMarker(props)
{
    const synthetic = getViewModel(props.store.getState()).synthetic;

    //console.log({synthetic});
    
    if (!synthetic)
    {
        return false;
    }

    const height = window.innerHeight;

    return (
        <div className="preview-marker" style={ { height: height + "px" } } />
    )
}

function lookupRenderFn(store)
{
    const viewModel = getViewModel(store.getState());
    const viewName = viewModel.name;

    let entry = renderFnCache[viewName];


    // if we have no view component or if the view component version is not the same as the view model version
    const shouldRerender = entry && entry.versionGUID !== viewModel.versionGUID;

    if (!entry || shouldRerender)
    {
        // if (shouldRerender)
        // {
        //     console.log("Rerender ", viewModel);
        // }

        // we (re)create the view component
        entry = viewRenderer.createRenderFunction(viewModel, componentService.getComponents());
        entry.versionGUID = viewModel.versionGUID;
        renderFnCache[viewName] = entry;
    }

    return entry.fn;
}

function getTitle(props)
{
    return props.model.title;
}

function viewStateSelector()
{

}

const ViewComponent = ComponentSubscription(
    class ViewComponent extends React.Component
    {
        static displayName = "ViewComponent";

        render()
        {
            const { store } = this.props;

            return (
                <FormProvider store={ store } update={ setScopeValue }>
                    { lookupRenderFn(store)(this) }
                    <PreviewMarker store={ store }/>
                </FormProvider>
            );
        }
    },
    (oldState, newState) =>
        getViewModel(oldState) === getViewModel(newState) &&
        getScopeGraph(oldState) === getScopeGraph(newState) &&
        getComponentData(oldState) === getComponentData(newState)
);

const viewService = {

    /**
     * Renders the given viewModel and viewData
     *
     *
     * @returns {Promise} resolves after rendering is done.
     *
     * @param store     redux store
     */
    render: function(store)
    {
        const rootElem = document.getElementById("root");
        if (!rootElem)
        {
            throw new Error("Missing #root DOM element");
        }

        return new Promise((resolve, reject) => {
            render(
                <Provider store={ store }>
                    <div>
                    <ViewComponent store={ store } />
                    { security.hasRole("ROLE_EDITOR") && <InPageEditor store={ store } /> }
                    <ThrobberComponent/>
                    { Dialog.render() }
                    </div>
                </Provider>,
                rootElem,
                resolve
            );
        });
    },

    renderFn: function()
    {
        const viewModel = getViewModel(store.getState());
        return renderFnCache[viewModel.name].src;
    }
};

/**
 * JsDoc type definition for our view model structure.
 *
 * @name ViewModel
 * @type {{
 *     name: String,
 *     version: String,
 *     title: String,
 *     content: object
 * }}
 */


export default viewService;

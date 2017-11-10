import React from "react"
import { Provider } from 'react-redux'

import security from "./security"
import store from "./store"
import { updateScopeCursor } from "../actions/scope"
import { getComponentData, getScopeGraph, getViewModel, getFormState } from "../reducers"
import { ThrobberComponent } from "../ui/throbber"
import Dialog from "../util/dialog"
import ComponentSubscription from "../util/ComponentSubscription"
import RTView from "../service/runtime-view-api"
import viewRenderer from "./view-renderer"
import ErrorBoundary from "../ui/ErrorBoundary"

const componentService = require("./component");

let InPageEditor = false;
if (process.env.USE_EDITOR)
{
    InPageEditor = require("../editor/InPageEditor");
}

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

let rtView = new RTView(null, store);

const ViewComponent = ComponentSubscription(
    class ViewComponent extends React.Component
    {
        static displayName = "ViewComponent";

        updateScope = (cursor,value) => {

            store.dispatch(
                updateScopeCursor(cursor, value)
            );
        };

        render()
        {
            const { store } = this.props;
            
            return (
                <div>
                    <ErrorBoundary>
                    {
                        lookupRenderFn(store)(this, rtView)
                    }
                    </ErrorBoundary>
                    <PreviewMarker store={ store }/>
                </div>
            );
        }
    },
    (oldState, newState) =>
        (
            getViewModel(oldState) === getViewModel(newState) &&
            getScopeGraph(oldState) === getScopeGraph(newState) &&
            getComponentData(oldState) === getComponentData(newState) &&
            getFormState(oldState) === getFormState(newState)
        ) ||
        // we ignore updates without formState
        getFormState(newState) === null
);

const viewService = {

    /**
     * Renders the given viewModel and viewData
     *
     * @param store     redux store
     *
     * @returns {ReactElement} rendered react element
     */
    render: function(store) {
        return (
            <Provider store={store}>
                <div>
                    <ViewComponent store={store}/>
                    {security.hasRole("ROLE_EDITOR") && InPageEditor && <InPageEditor store={store}/>}
                    <ThrobberComponent/>
                    {Dialog.render()}
                </div>
            </Provider>
        );
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

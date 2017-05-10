export const COMPONENT_UPDATE = "UPDATE_COMPONENT";

import store from "../service/store"
import ajax from "../service/ajax"
import appNavHistory from "../service/app-history"
import uri from "../util/uri"
import update from "react-addons-update"

import { getComponentVars, getViewModel } from "../reducers"

function updateComponentVars(id, componentData)
{
    return {
        type: COMPONENT_UPDATE,
        componentId: id,
        componentData: componentData
    };
}

/**
 * Does a quick conversion of internal view model to external view model by
 * blocking the internal-format expression fields.
 *
 * For an in-memory model structure, this should be done with "util/to-external.js".
 * 
 * @param k     key
 * @param v     value
 * @returns {*}
 */
function stripInternalFormatAttributes(k, v)
{
    if (k !== "exprs" && k !== "titleExpr")
    {
        return v;
    }
}

export function updateComponent(id, vars)
{
    return (dispatch, getState, Services) =>
    {
        if (!id)
        {
            throw new Error("Need id: " + id);
        }

        if (!vars)
        {
            throw new Error("Need vars : " + vars);
        }

        const state = store.getState();
        const currentVars = getComponentVars(state, id);

        if (!currentVars)
        {
            throw new Error("No component data for component '" + id +"'");
        }

        vars = update(currentVars, {
            $merge: vars
        });

        const opts = {
            url: uri(window.location.href, {
                _id : id,
                _vars: JSON.stringify(vars)
            }, true),
            headers: {
                "X-ceed-Update": "true"
            }
        };

        const currentViewModel = getViewModel(state);

        if (currentViewModel.synthetic)
        {
            opts.method =  "POST";
            opts.contentType = "application/json";
            opts.data = JSON.stringify(currentViewModel, stripInternalFormatAttributes);
            opts.headers['X-ceed-Preview'] = "true";
        }

        return ajax(opts)
            .then((componentData) =>
            {
                
                dispatch(
                    updateComponentVars(id, componentData)
                );

                appNavHistory.update(store.getState());
            })
            .catch(function (err)
            {
                console.error("Error updating component '" + id + ", vars = " + JSON.stringify(vars), err);
                return Promise.reject(err);
            });
    }
}

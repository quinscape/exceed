import update from "immutability-helper";

import appNavHistory from "../service/app-history";
import Scope from "../service/scope";

import { getComponentVars } from "../reducers/component";
import { convertComponentData } from "../util/convert";
import { fetch } from "./view";

export const COMPONENT_UPDATE = "COMPONENT_UPDATE";

export function updateComponent(componentId, vars, noMerge)
{
    return (dispatch, getState) =>
    {
        if (!componentId)
        {
            throw new Error("Need id: " + componentId);
        }

        if (!vars)
        {
            throw new Error("Need vars : " + vars);
        }

        const state = getState();
        const currentVars = getComponentVars(state, componentId);

        if (!currentVars)
        {
            throw new Error("No component data for component '" + componentId +"'");
        }

        if (!noMerge)
        {
            vars = update(currentVars, {
                $merge: vars
            });
        }

        return fetch(getState(),
            {
                method:  "POST",
                dataType: "JSON",
                data: {
                    _id : componentId,
                    _vars: vars,
                    contextUpdate: Scope.getScopeDelta()

                },
                headers: {
                    "X-ceed-Update": "true"
                }
            })
            .then((componentData) =>
            {
                dispatch({
                    type: COMPONENT_UPDATE,
                    componentId,
                    componentData: {
                        vars: componentData.vars,
                        data: convertComponentData(componentData.data)
                    }
                });

                appNavHistory.update(getState());
            })
            .catch(function (err)
            {
                console.error("Error updating component '" + componentId + ", vars = " + JSON.stringify(vars), err);
                return Promise.reject(err);
            });
    }
}

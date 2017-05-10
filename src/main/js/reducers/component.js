import assign from "object-assign"

import { COMPONENT_UPDATE } from "../actions/component"

export default function(componentState = {}, action)
{
    if (action.type === COMPONENT_UPDATE)
    {
        const newState = assign({}, componentState);
        newState[action.componentId] = action.componentData;
        return newState;
    }
    return componentState;
}

export function getComponentData(state)
{
    return state.component;
}

export function getComponentVars(state, id)
{
    const componentEntry = getComponentData(state)[id];
    return componentEntry ? componentEntry.vars : null;
}

export function getComponentInjections(state, id)
{
    const componentEntry = getComponentData(state)[id];
    return componentEntry ? componentEntry.data : null;
}




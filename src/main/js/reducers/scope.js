
import assign from "object-assign"

import { getMeta } from "../reducers/meta"
import { SCOPE_SET } from "../actions/scope"

export default function(scope = {}, action)
{
    if (action.type === SCOPE_SET)
    {
        const changed = action.path[0];

        let dirty = scope.dirty || {};

        // was the scope value not marked dirty before?
        if (!dirty[changed])
        {
            // mark dirty
            dirty = assign({}, dirty);
            dirty[changed] = true;
        }

        return {
            graph: action.graph,
            dirty: dirty
        };
    }

    return scope;
}

/**
 * Returns the complete data graph encapsulating all scoped values.
 *
 * @param state     redux state
 *
 * @returns {DataGraph} data graph object
 */
export function getScopeGraph(state)
{
    return state.scope.graph;
}

export function getScopeDirty(state)
{
    return state.scope.dirty;
}

export function getScopeDeclarations(state)
{
    return getMeta(state).scope.declarations;
}

export function getScopeKey(state)
{
    return getMeta(state).scope.key;
}

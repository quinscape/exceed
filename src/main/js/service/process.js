import assign from "object-assign";
import Scope from "./scope";
import uri from "../util/uri";
import sys from "../sys";
import store from "./store"

import { executeTransition } from "../actions/view"
import { getLocation } from "../reducers"

import RTView from "./runtime-view-api";

function renderURI(locInfo, transition)
{
    const params = {
        stateId: locInfo.params.stateId,
        _trans: transition
    };

    return uri( "/app/" + sys.appName + locInfo.routingTemplate, params)
}

export default {
    transition: function(name, data)
    {
        const state = store.getState();

        const locInfo = getLocation(state);

        //const names = getScopeQueryRefs(state).concat(getScopeViewRefs(state));

        return store.dispatch(
            executeTransition({
                url: renderURI(locInfo, name),
                data: {
                    objectContext: data,
                    contextUpdate: Scope.getScopeUpdate()
                },
                urlProvider: function (xhr, data)
                {
                    const locInfo = getLocation(data);
                    return renderURI(locInfo);
                }
            })
        );
    },
    scope: RTView.prototype.scope
}

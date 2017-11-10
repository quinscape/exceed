import Scope from "./scope";
import uri from "../util/uri";
import sys from "../sys";
import store from "./store"

import { executeTransition } from "../actions/view"
import { getLocation, getScopeDeclarations } from "../reducers"

import Dialog from "../util/dialog";
import { getViewStateTransition } from "../reducers/meta"
import i18n from "../service/i18n"

import RTView from "./runtime-view-api";

function renderURI(locInfo, transition)
{
    const params = {
        stateId: locInfo.params.stateId,
        _trans: transition
    };

    return uri( "/app/" + sys.appName + locInfo.routingTemplate, params)
}

const processService = {
    transition: function(transitionName, context)
    {
        const transitionModel = getViewStateTransition(store.getState(), transitionName);
        if (!transitionModel)
        {
            throw new Error("No transition '" + transitionName + "' exists");
        }

        const { confirmation } = transitionModel;

        let promise;
        if (confirmation)
        {
            promise = Dialog.prompt({
                title: confirmation.title,
                text: confirmation.message,
                choices: [ i18n("Cancel"), confirmation.okLabel ],
                properties: []
            });
        }
        else
        {
            promise = Promise.resolve();
        }

        return promise.then(function(result)
            {
                //console.log("CONFIRMATION", result);

                if (result && result.choice !== 1)
                {
                    return;
                }

                const state = store.getState();
                const locInfo = getLocation(state);

                const scopeDeclarations = getScopeDeclarations(state);
                const contextUpdate = Scope.getScopeDelta();

                if (context)
                {
                    for (let name in context)
                    {
                        if (context.hasOwnProperty(name))
                        {
                            const obj = context[name];

                            const decl = scopeDeclarations[name];
                            if (decl)
                            {
                                const model = decl.model;
                                if (model.type === "DomainType")
                                {
                                    if ((model.typeParam === null || model.typeParam === obj._type))
                                    {
                                        contextUpdate[name] = obj;
                                        delete context[name];
                                    }
                                }
                            }
                        }
                    }
                }

                return store.dispatch(
                    executeTransition({
                        url: renderURI(locInfo, transitionName),
                        data:
                            {
                                context,
                                contextUpdate
                            },
                        urlProvider: function (xhr, data) {
                            // render without transition parameter
                            return renderURI(getLocation(data));
                        }
                    })
                );
            }
        );
    },
    scope: RTView.prototype.scope
};

export default processService

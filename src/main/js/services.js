import StoreHolder from "./service/store";

import { getScopeGraph } from "./reducers/scope"
import { updateScope } from "./actions/scope"
import { getComponentData } from "./reducers/component"
import { getViewModel } from "./reducers/meta"
import { findComponents } from "./util/component-util"
import { HISTORY } from "./util/history-middleware"

import { evaluateEmbedded } from "./util/startup"

/**
 * Services module exposed as "Exceed" to the browser environment
 */
const Services = {
    store: function ()
    {
        return StoreHolder;
    },
    state: function ()
    {
        return StoreHolder.getState();
    },
    scope: function (name, value)
    {
        if (value !== undefined)
        {
            StoreHolder.dispatch(
                updateScope(typeof name === "string" ? [ name ] : name, value)
            )
        }
        else
        {
            const scopeGraph = getScopeGraph(this.state());
            if (typeof name === "string")
            {
                if (!scopeGraph.columns[name])
                {
                    console.warn("No scoped value '" + name + "'");
                }
                return scopeGraph.rootObject[name];
            }
            else
            {
                return scopeGraph.rootObject;
            }
        }
    },
    rawData: function ()
    {
        return evaluateEmbedded("root-data", "x-ceed/view-data");
    },

    component: function (elementId)
    {
        const componentData = getComponentData(this.state());
        if (typeof elementId === "string")
        {
            return componentData[elementId];
        }
        else
        {
            return componentData;
        }
    },

    findComponents: function (predicate) {
        return findComponents( getViewModel(this.state()) , predicate);
    },

    history: function () {
        console.group("Redux history");
        console.table(HISTORY);
        console.groupEnd();
    }
};

const instances = {};

function lazyInit(name, fn)
{
    Services[name] = () =>
    {
        const instance = instances[name];

        if (!instance)
        {
            return instances[name] = fn()
        }
        return instance;
    }
}

lazyInit("sys", () => require("./sys"));
lazyInit("uri", () => require("./util/uri"));
lazyInit("hub", () => require("./service/hub"));
lazyInit("componentService", () => require("./service/component"));
//lazyInit("converter", () => require("./service/property-converter"));
lazyInit("action", () => require("./service/action"));
lazyInit("view", () => require("./service/view").default);
//lazyInit("history", () => require("./service/app-history").default);
//azyInit("actions", () => require("./actions").default);
//lazyInit("undo", () => require("./editor/UndoManager").default);
//lazyInit("scopeService", () => require("./service/scope").default);

export  default Services

/**
 * This module is exported as package "exceed-services" for external code.
 *
 * @type {{Hub: (Hub|exports|module.exports)}}
 * @module "exceed-services"
 */

import StoreHolder from "./service/store";

const Services = {
    store: function ()
    {
        return StoreHolder;
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
            console.log("LAZY INIT", name);
            return instances[name] = fn()
        }
        return instance;
    }
}

lazyInit("sys", () => require("./sys"));
lazyInit("uri", () => require("./util/uri"));
lazyInit("hub", () => require("./service/hub"));
lazyInit("component", () => require("./service/component"));
lazyInit("converter", () => require("./service/property-converter"));
lazyInit("action", () => require("./service/action"));
lazyInit("view", () => require("./service/view").default);
lazyInit("history", () => require("./service/app-history").default);
lazyInit("actions", () => require("./actions").default);
lazyInit("editorActions", () => require("./actions/editor").default);
lazyInit("undo", () => require("./editor/UndoManager").default);

/**
 *
 * @type {{store: function, hub: function, component: function, converter: function, action: function, view: function}}
 */
export default Services;

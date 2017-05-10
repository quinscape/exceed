import { Promise } from "es6-promise-polyfill"
import actionService from "../service/action"
import store from "../service/store"
import Throbber from "../ui/throbber"

import { updateComponent } from "../actions/component"

function defaultError(err)
{
    console.error(err);
    return Promise.reject(err);
}

function throbberError(e)
{
    Throbber.disable();
    return defaultError(e);
}

/**
 * Internal runtime API for action expressions. de.quinscape.exceed.runtime.service.ActionExpressionRenderer
 * transforms the expression language action expressions to promise chains targeting this API.
 * @type {{action: module.exports.action, observe: module.exports.observe, uri: (uri|exports|module.exports)}}
 */
module.exports = {

    /**
     * Returns a promise for the given action execution if the optional name is set, it will be copied to
     * identifying "action" property, otherwise the model must already contain the "action" property.
     *
     * @param model     {object} JSON action model
     * @param name      {?name} optional name parameter.
     * @returns {Promise}
     */
    action: function (model, name)
    {
        // (name, model)?
        if (name !== undefined)
        {
            // yes -> make sure the given object has the right action attribute
            model.action = name;
        }

        if (!model.action)
        {
            return Promise.reject(new Error("No action property set"));
        }

        return actionService.execute(model);
    },

    /**
     * Default action error handling.
     *
     * @param promise   promise to observe
     */
    observe: function (promise)
    {
//        console.log("enter observe");

        var enableThrobber = require("../cando").ajax;
        enableThrobber && Throbber.enable();

        if (enableThrobber)
        {
            return promise.then(Throbber.disable, throbberError);
        }
        else
        {
            return promise.catch(defaultError);
        }
    },


    update : function (id, vars)
    {
        store.dispatch(
            updateComponent(
                id,
                vars
            )
        );
    },

    uri: require("../util/uri")
};



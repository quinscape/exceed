import { Promise } from "es6-promise-polyfill"
import actionService from "../service/action"
import store from "../service/store"
import Throbber from "../ui/throbber"

import { updateComponent } from "../actions/component"

const domainService = require("../service/domain");
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

function getStateMachineState(stateMachine, stateName)
{
    const array = stateMachine.states[stateName];
    if (!array)
    {
        throw new Error("'" + stateName + "' is no valid state in state machine '" + stateMachine.name + "'");
    }
    return array;
}
/**
 * Internal runtime API for action expressions. de.quinscape.exceed.runtime.service.ActionExpressionRenderer
 * transforms the expression language action expressions to promise chains targeting this API.
 * @type {{action: module.exports.action, observe: module.exports.observe, uri: (uri|exports|module.exports)}}
 */
export default {

    /**
     * Returns a promise for the given action execution if the optional name is set, it will be copied to
     * identifying "action" property, otherwise the model must already contain the "action" property.
     *
     * @param model     {object} JSON action model
     * @param name      {?name} optional name parameter.
     * @returns {Promise}
     */
    action: function (name, args)
    {
        return actionService.execute(name, args);
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

    isValidTransition: function (stateMachineName, from, to)
    {
        const stateMachine = domainService.getStateMachine(stateMachineName);
        if (!stateMachine)
        {
            throw new Error("Could not find state machine '" + stateMachineName + "'");
        }
        const array = getStateMachineState(stateMachine, from);
        getStateMachineState(stateMachine, to);

        return array.indexOf(to) >= 0;
    },

    uri: require("../util/uri")
}



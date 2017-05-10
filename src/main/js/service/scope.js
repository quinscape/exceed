import store from "../service/store"
import * as domainService from "../service/domain"
import { getScopeGraph, getScopeDirty, getDomainTypes } from "../reducers"

import { DataGraph, getColumnType } from "../domain/graph"
import DataCursor from "../domain/cursor"

const keys = require("../util/keys");

/**
 * Provides access to the scoped values available in the current context.
 *
 * This module is one of the target for static call detection via babel-plugin-track-usage
 *
 * @type {{list: Scope.list, object: Scope.object, property: Scope.property}}
 */
const Scope = {

    property: function(name)
    {
        return Scope.propertyCursor(name).get();
    },

    propertyCursor: function(name)
    {
        const state = store.getState();
        const scope = DataGraph(
            getScopeGraph(state)
        );
        return new DataCursor(domainService.getDomainTypes(), scope, [ name ]);
    },

    propertyType: function (name)
    {
        const state = store.getState();
        const scope = DataGraph(
            getScopeGraph(state)
        );

        return getColumnType(scope, name);
    },

    getScopeUpdate: function ()
    {
        const state = store.getState();
        const names = keys(getScopeDirty(state));

        const values = {};

        const scope = getScopeGraph(state);
        const domainTypes = getDomainTypes(state);                                      

        for (let i = 0; i < names.length; i++)
        {
            const name = names[i];
            values[name] = new DataCursor(domainTypes, scope, [ name ]).get();
        }
        return values;
    }
};

export default Scope;

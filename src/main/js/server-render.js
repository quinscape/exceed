// setImmediate from promises is provided on the server-side by a java host object
import "es6-shim"
import { Promise } from "es6-promise-polyfill"

import { renderToString } from "react-dom/server"

import bindToGlobal from "./util/bind-to-global"

import createStore from "./create-store"
import StoreHolder from "./service/store";

import viewService from "./service/view"
import security from "./service/security"
import sys from "./sys"
import { getAppName, getContextPath, getAuthentication } from "./reducers/meta"


import rootReducer from "./reducers/index"
import { hydrateAppState } from "./actions/reset"

const componentService = require("./service/component");
componentService.registerFromRequireContext(
    require.context("./components/std/", true, /\.js(on)?$/)
);

/**
 * All properties of this object are bound to the global object as non-writable property for easy access
 */
module.exports = bindToGlobal({

    _renderToString: function(viewDataJSON)
    {

        const initialState = JSON.parse(viewDataJSON);
        StoreHolder._init(null, initialState);

//console.log("Initial state", initialState);

        const store = createStore(rootReducer, initialState);
        StoreHolder._init(store, null);

        initialState.meta.isHydration = true;

        store.dispatch(hydrateAppState(initialState));

        const state = store.getState();
        const auth = getAuthentication(state);
        security.init(auth.userName, auth.roles);

        sys.init( getContextPath(state), getAppName(state));

        return renderToString(
            viewService.render(store)
        );
    },

    Promise: Promise

});

import ReactDOM from "react-dom"
import { Promise } from "es6-promise-polyfill"
import { evaluateEmbedded, findBundles } from "./util/startup"
import { hydrateAppState } from "./actions/reset"
import rootReducer from "./reducers/index"

import domready from "domready"

import createStore from "./create-store"

import StoreHolder from "./service/store";
import {
    getActionNames,
    getAppName,
    getAuthentication,
    getContextPath,
    getDomainData
} from "./reducers"
import security from "./service/security"
import Services from "./services"
import svgLayout from "./gfx/svg-layout"

const initialState = evaluateEmbedded("root-data", "x-ceed/view-data");
StoreHolder._init(null, initialState);

//console.log("Initial state", initialState);

const store = createStore(rootReducer, initialState);
StoreHolder._init(store, null);

const state = StoreHolder.getState();

const sys = require("./sys");
const domainService = require("./service/domain");
const actionService = require("./service/action");

sys.init( getContextPath(state), getAppName(state));
domainService.init(getDomainData(state));

const actions = getActionNames(state);
//console.info("Server actions", actions);
actionService.initServerActions(actions);
actionService.registerFromRequireContext(
    require.context("./action/", true, /\.js$/)
);

const componentService = require("./service/component");
componentService.registerFromRequireContext(
    require.context("./components/std/", true, /\.js(on)?$/)
);


const auth = getAuthentication(initialState);
security.init(auth.userName, auth.roles);

const viewService = require("./service/view").default;

const appNavHistory = require("./service/app-history").default;


domready(function ()
{

    //console.log("DOMREADY");

    const rootElem = document.getElementById("root");
    const isHydration = !!rootElem.innerHTML;
    if (!rootElem)
    {
        throw new Error("Missing #root DOM element");
    }

    initialState.meta.isHydration = isHydration;

    store.dispatch(hydrateAppState(initialState));

    // set correct public path for dynamic module loading.
    const scriptResourcePath = sys.contextPath + "/res/" + sys.appName + "/js/";
    // noinspection JSUndeclaredVariable
    __webpack_public_path__ = scriptResourcePath;

    // async setup
    Promise.all([
        svgLayout.init()
    ]).then(function ()
    {
        appNavHistory.init();
        appNavHistory.update(store.getState());

        return new Promise(function (resolve, reject) {

            const element = viewService.render(store);
            if (isHydration)
            {
                ReactDOM.hydrate(
                    element,
                    rootElem,
                    resolve
                );
            }
            else
            {
                ReactDOM.render(
                    element,
                    rootElem,
                    resolve
                );
            }


        });

    })
    .then(function ()
    {
        const scripts = findBundles(scriptResourcePath);
        console.info("READY: Loaded " + scripts.join(", "), "( "+ new Date().toISOString() +" )");
    })
    // .catch(function (e)
    // {
    //     console.error(e);
    // });
});

// This export will be available as "Exceed" in the browser environment of the running application
// traditional export to not have a .default in the browser env
// noinspection JSUnusedGlobalSymbols
module.exports = Services;

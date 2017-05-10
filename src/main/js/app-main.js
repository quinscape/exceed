//
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols
const MouseTrap = require("./util/global-undo");
import { Promise } from "es6-promise-polyfill"
import { evaluateEmbedded, findBundles } from "./util/startup"

import domready from "domready"

import createStore from "./create-store"

import StoreHolder from "./service/store";
const initialState = evaluateEmbedded("root-data", "x-ceed/view-data");
StoreHolder._init(null, initialState);

import { getActionNames, getAppName, getAuthentication, getConnectionId, getContextPath, getDomain} from "./reducers"
import security from "./service/security"

//console.log("Initial state", initialState);

import Services from "./services"

const store = createStore(rootReducer, initialState);
StoreHolder._init(store, null);

const auth = getAuthentication(initialState);
security.init(auth.userName, auth.roles);

const viewService = require("./service/view").default;

const sys = require("./sys");

const componentService = require("./service/component");
const actionService = require("./service/action");

const domainService = require("./service/domain");
const hub = require("./service/hub");
const appNavHistory = require("./service/app-history").default;

const svgLayout = require("./gfx/svg-layout");

const hotReload = require("./service/hotreload");

import rootReducer from "./reducers"

componentService.registerFromRequireContext(
    require.context("./components/std/", true, /\.js(on)?$/)
);

actionService.registerFromRequireContext(
    require.context("./action/", true, /\.js$/)
);

domready(function ()
{

    //console.log("DOMREADY");

    const state = StoreHolder.getState();

    sys.init( getContextPath(state), getAppName(state));
    domainService.init(getDomain(state));

    const actions = getActionNames(state);
    //console.info("Server actions", actions);
    actionService.initServerActions(actions);

    // set correct public path for dynamic module loading.
    const scriptResourcePath = sys.contextPath + "/res/" + sys.appName + "/js/";
    __webpack_public_path__ = scriptResourcePath;
    
    // async setup
    Promise.all([
        svgLayout.init(),
        hub.init(getConnectionId(state))
    ]).then(function ()
    {
        appNavHistory.init();
        appNavHistory.update(store.getState());
        return viewService.render(store);
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


module.exports = Services;

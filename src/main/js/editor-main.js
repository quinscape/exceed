//
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols
import aceLoader from "./editor/ace-loader"
import Event from "./util/event";
import { Promise } from "es6-promise-polyfill"
import { getEditorView } from "./reducers/editor/editorView"

import React from "react"
import { Provider } from "react-redux"
import { render } from "react-dom"

import rootReducer from "./reducers/editor"

import { evaluateEmbedded, findBundles } from "./util/startup"

import domready from "domready"

import createStore from "./create-store"

import StoreHolder from "./service/store";
import { getAppName, getAuthentication, getConnectionId, getContextPath, getDomainData } from "./reducers"
import { syncEditor } from "./actions/editor/editorView"
import security from "./service/security"

import UndoManager from "./editor/UndoManager"
import Services from "./services"
import sys from "./sys";

import domainService from "./service/domain"
import i18n from "./service/i18n"
import svgLayout from "./gfx/svg-layout"

const initialState = evaluateEmbedded("root-data", "x-ceed/view-data");
StoreHolder._init(null, initialState);

//console.log("Initial state", initialState);

const store = createStore(rootReducer, initialState, [ UndoManager.middleWare ]);
StoreHolder._init(store, null);

const auth = getAuthentication(initialState);
security.init(auth.userName, auth.roles);

const Editor = require("./editor/Editor").default;

const hub = require("./service/hub");
const editorNavHistory = require("./editor/nav-history").default;

domready(function ()
{
    //console.log("DOMREADY");

    const state = StoreHolder.getState();

    sys.init( getContextPath(state), getAppName(state));
    domainService.init(getDomainData(state));

    // set correct public path for dynamic module loading.
    const scriptResourcePath = sys.contextPath + "/res/" + sys.appName + "/js/";
    // noinspection JSUndeclaredVariable
    __webpack_public_path__ = scriptResourcePath;

    // async setup
    Promise.all([
        aceLoader.load(),
        svgLayout.init(),
        hub.init(getConnectionId(state))
    ]).then(function ( [ ace ] )
    {
//        console.log(ace);

        const state = store.getState();
        editorNavHistory.init();
        editorNavHistory.update(getEditorView(state));

        UndoManager.update(state);

        if (typeof window !== "undefined")
        {
            Event.add(window, "beforeunload", ev =>
            {
                if (!UndoManager.isClean())
                {
                    const message = 'WARNING: Lose unsaved changes in Editor?';
                    ev.returnValue = message;
                    return message;
                }
            }, true);
        }

        store.dispatch(
            syncEditor()
        );

        return new Promise((resolve,reject) =>
            render(
                <Provider store={ store }>
                    <Editor />
                </Provider>,
                document.getElementById("root"),
                resolve
            )
        );
    })
    .then(function ()
    {
        const scripts = findBundles(scriptResourcePath);
        console.info( i18n("READY: Loaded {0} ( {1} )", scripts.join(", "), new Date().toISOString()));
    })
    // .catch(function (e)
    // {
    //     console.error(e);
    // });
});


export default Services;

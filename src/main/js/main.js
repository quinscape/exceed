//
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols
const MouseTrap = require("./util/global-undo");

const Promise = require("es6-promise-polyfill").Promise;

const domready = require("domready");

var model = evaluateEmbedded("root-model", "x-ceed/view-model");
var data = evaluateEmbedded("root-data", "x-ceed/view-data");

window._exceed_initial_data = data;
const viewService = require("./service/view");
viewService._init(data);

const sys = require("./sys");

const componentService = require("./service/component");
const actionService = require("./service/action");
const security = require("./service/security");

const domainService = require("./service/domain");
const hub = require("./service/hub");

const svgLayout = require("./gfx/svg-layout");

const hotReload = require("./service/hotreload");

componentService.registerFromRequireContext(
    require.context("./components/std/", true, /\.js(on)?$/)
);

var prodCtx = false;

if (process.env.NODE_ENV !== "production")
{
    prodCtx = require.context("./components/editor/", true, /\.js(on)?$/)
}

if (prodCtx)
{
    componentService.registerFromRequireContext(
        prodCtx
    );
}


actionService.registerFromRequireContext(
    require.context("./action/", true, /\.js$/)
);

function evaluateEmbedded(elemId, mediaType)
{
    var elem = document.getElementById(elemId);
    if (!elem || elem.getAttribute("type") !== mediaType)
    {
        throw new Error("#" + elemId + " is not a script of type '" + mediaType + "': " + elem);
    }

    return JSON.parse(elem.innerHTML);
}

domready(function ()
{
    //console.log("DOMREADY");

    var bodyData = document.body.dataset;
    security.init(bodyData.login,bodyData.roles);


    var systemInfo  = evaluateEmbedded("system-info", "x-ceed/system-info");

    sys.init( systemInfo.contextPath, bodyData.appName);
    domainService.init(data._exceed.applicationDomain);

    console.info("Server actions", systemInfo.actions);
    actionService.initServerActions(systemInfo.actions);

    // async setup
    Promise.all([
        hub.init(bodyData.connectionId),
        svgLayout.init()
    ]).then(function ()
    {
        return viewService.render(model, data);
    })
    .catch(function (e)
    {
        console.error(e);
    });
});

module.exports = require("./services");

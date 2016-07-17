//
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols
const MouseTrap = require("./util/global-undo");

const Promise = require("es6-promise-polyfill").Promise;

const bulk = require("bulk-require");
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

const componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);
componentService.registerBulk(componentsMap.components);

const clientActionMap = bulk(__dirname, ["action/**/*.js"]);
actionService.registerBulk(clientActionMap.action);

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
    security.init(bodyData.roles);


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


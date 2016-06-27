var Promise = require("es6-promise-polyfill").Promise;

var bulk = require("bulk-require");
var domready = require("domready");

var sys = require("./sys");

var componentService = require("./service/component");
var actionService = require("./service/action");
var security = require("./service/security");
var viewService = require("./service/view");
var domainService = require("./service/domain");
var hub = require("./service/hub");

var svgLayout = require("./util/svg-layout");

var hotReload = require("./service/hotreload");

var componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);
componentService.registerBulk(componentsMap.components);

var clientActionMap = bulk(__dirname, ["action/**/*.js"]);
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

    var model = evaluateEmbedded("root-model", "x-ceed/view-model");
    var data = evaluateEmbedded("root-data", "x-ceed/view-data");
    var systemInfo  = evaluateEmbedded("system-info", "x-ceed/system-info");

    sys.init( systemInfo.contextPath, bodyData.appName);
    domainService.init(data._exceed.applicationDomain);

    console.info("Server actions", systemInfo.actions);
    actionService.initServerActions(systemInfo.actions);

    // async setup
    Promise.all([
        svgLayout.init(),
        hub.init(bodyData.connectionId)
    ]).then(function ()
    {

        return viewService.render(model, data);
    })
    .catch(function (e)
    {
        console.error(e);
    });
});


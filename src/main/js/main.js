var Promise = require("es6-promise-polyfill").Promise;

var bulk = require("bulk-require");
var domready = require("domready");

var componentService = require("./service/component");
var actionService = require("./service/action");
var security = require("./service/security");
var render = require("./service/render");

var svgLayout = require("./util/svg-layout");

var hotReload = require("./service/hotreload");

var componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);
componentService.registerBulk(componentsMap);

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

var currentViewName;
var currentViewData;

domready(function ()
{
    security.init(document.body.dataset.roles);

    var contextPath = document.body.dataset.contextPath;

    window.appName = document.body.dataset.appName;
    window.contextPath = contextPath;

    var model = evaluateEmbedded("root-model", "x-ceed/view-model");
    var data = evaluateEmbedded("root-data", "x-ceed/view-data");
    var systemInfo  = evaluateEmbedded("system-info", "x-ceed/system-info");

    currentViewName = model.name;
    currentViewData = data.data;

    //console.log("currentViewData", JSON.stringify(currentViewData, null, "  "));

    var serverActions = systemInfo.actions;
    console.info("Server actions", serverActions);

    actionService.initServerActions(serverActions);

    //actionService.execute([
    //    {
    //        action: "sleep",
    //        time: 10000
    //    },
    //    {
    //        action: "ping"
    //    }
    //], {value:1});

    // async setup
    Promise.all([
        svgLayout.init()
    ]).then(function ()
    {
        if (process.env.NODE_ENV !== "production" && security.hasRole("ROLE_EDITOR"))
        {
            hotReload.enablePolling();
        }

        return render.render(model, currentViewData);
    })
    .catch(function (e)
    {
        console.error(e);
    });
});


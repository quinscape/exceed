var React = require("react");

var Promise = require("es6-promise-polyfill").Promise;

var bulk = require("bulk-require");
var domready = require("domready");

var componentService = require("./service/component");
var actionService = require("./service/action");
var ajax = require("./service/ajax");
var security = require("./service/security");
var render = require("./service/render");

var ValueLink = require("./util/value-link");

var svgLayout = require("./util/svg-layout");

var Alert = require("./ui/Alert");

var componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);
componentService.registerBulk(componentsMap);

var clientActionMap = bulk(__dirname, ["action/**/*.js"]);
actionService.registerBulk(clientActionMap.action);

const ACTIVITY_TIMEOUT = 60000;

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
var pollingEnabled = false;

// are we currently waiting for a poll response?
var polling = false;
var timeout = false;
var lastActivity = 0;

function timestampURL(href)
{
    var marker = ";reload=" + Date.now();
    var pos = href.indexOf(";");
    if (pos < 0)
    {
        href += marker;
    }
    else
    {
        href = href.substr(0, pos) + marker;
    }

    return href;
}

if (process.env.NODE_ENV !== "production" )
{
    var pollChanges = function()
    {
        if (!pollingEnabled || polling)
        {
            return;
        }

        polling = true;
        ajax({
            url: contextPath + "/reload/" + appName
        }).then(function (model)
        {
            polling = false;
            //alert("CHANGE" + JSON.stringify(model));

            if (model._type === "view.View")
            {

                if (render.getViewModel().name === model.name)
                {
                    console.log("currentViewData", currentViewData);
                    render.render(model, currentViewData)
                        .then(pollChanges)
                        .catch(function (e)
                        {
                            console.error(e);
                        });
                    return;
                }
            }
            else if (model._type === "change.Shutdown")
            {
                pollingEnabled = false;
                alert("Server has been shut down.\nYou might want to reload");
                return;
            }
            else if (model._type === "change.CodeChange")
            {
                console.log("CodeChange");

                var elem = document.createElement("script");
                elem.setAttribute("src", timestampURL(contextPath + "/res/" + appName + "/js/main.js"));

                var head = document.getElementsByTagName("head")[0];
                head.appendChild(elem);

            }
            else if (model._type === "change.StyleChange")
            {
                var link = document.getElementById("application-styles");
                link.href = timestampURL(link.href);

                console.log("StyleChange")
            }

            var now = Date.now();
            if (now - lastActivity < ACTIVITY_TIMEOUT)
            {
                pollChanges();
            }
            else
            {
                console.info("activity timeout, polling stops..");
                timeout = true;
            }

        }).catch(function (err)
        {
            console.log(err);
        })
    };

    var activity = function(ev)
    {
        lastActivity = Date.now();
        if (timeout)
        {
            timeout = false;
            console.info("Resuming polling on activity..");
            pollChanges();
        }
    }

}
function enablePolling()
{
    if (process.env.NODE_ENV !== "production" )
    {
        pollingEnabled = true;

        document.body.addEventListener("keydown", activity, true);
        document.body.addEventListener("mousemove", activity, true);

        pollChanges();
    }
}

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
    //        action: "pong",
    //        increment: 1
    //    },
    //    {
    //        action: "ping"
    //    },
    //    {
    //        action: "pong",
    //        increment: 2
    //    },
    //    {
    //        action: "ping"
    //    }
    //], {value:1})

    // async setup
    Promise.all([
        svgLayout.init()
    ]).then(function ()
    {
        if (process.env.NODE_ENV !== "production" && security.hasRole("ROLE_EDITOR"))
        {
            enablePolling();
        }

        return render.render(model, currentViewData);
    })
    .catch(function (e)
    {
        console.error(e);
    });
});


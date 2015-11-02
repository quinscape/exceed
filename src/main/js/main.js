var React = require("react/addons");
var ReactDOM = require("react-dom");

var Promise = require("es6-promise-polyfill").Promise;

var bulk = require("bulk-require");
var componentService = require("./service/component");
var viewService = require("./service/view");
var ajax = require("./service/ajax");
var security = require("./service/security");

var ValueLink = require("./util/value-link");

var svgLayout = require("./util/svg-layout");

var Alert = require("./ui/Alert");
var ViewComponent = require("./ui/ViewComponent");

var componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);

componentService.registerBulk(componentsMap);

// log available components
//console.dir(componentService.getComponents());


var domready = require("domready");
window.domready = domready;
function evaluateEmbedded(elemId, mediaType)
{
    var elem = document.getElementById(elemId);
    if (!elem || elem.getAttribute("type") !== mediaType)
    {
        throw new Error("#" + elemId + " is not a script of type '" + mediaType + "': " + elem);
    }

    return JSON.parse(elem.innerHTML);
}

var rootElem;
var currentViewName;
var currentViewData;


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


function pollChanges()
{
    ajax({
        url: contextPath + "/reload/" + appName
    }).then(function (model)
    {
        //alert("CHANGE" + JSON.stringify(model));

        if (model._type === "view.View")
        {

            if (currentViewName === model.name)
            {
                console.log("currentViewData", currentViewData);

                ReactDOM.render(React.createElement(ViewComponent, {
                    model: model,
                    componentData: currentViewData
                }), rootElem, pollChanges);
                return;
            }
        }
        else if (model._type === "change.Shutdown")
        {
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

        pollChanges();
    }).catch(function (err)
    {
        console.log("Error update", err);
    })
}
domready(function ()
{
    security.init();

    var contextPath = document.body.dataset.contextPath;
    var appName = document.body.dataset.appName;

    rootElem = document.getElementById("root");

    window.appName = appName;
    window.contextPath = contextPath;

    var path = location.pathname.substring(contextPath.length);

    var model = evaluateEmbedded("root-model", "x-ceed/view-model");
    var data = evaluateEmbedded("root-data", "x-ceed/view-data");

    currentViewName = model.name;
    currentViewData = data.data;

    //console.log("currentViewData", JSON.stringify(currentViewData, null, "  "));

    Promise.all([
        svgLayout.init()
    ]).then(function (sizes)
    {
        ReactDOM.render( React.createElement(ViewComponent, {
            model: model,
            componentData: currentViewData
        }), rootElem);

        pollChanges();
    });

});

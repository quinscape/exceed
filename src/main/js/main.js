var bulk = require("bulk-require");
var componentService = require("./service/component");
var viewService = require("./service/view");

var Alert = require("./ui/Alert");

var componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);

componentService.registerBulk(componentsMap);

console.dir(componentService.getComponents());

var domready = require("domready");
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
    var contextPath = document.body.dataset.contextPath;

    var root = document.getElementById("root");

    window.contextPath = contextPath;

    var path = location.pathname.substring(contextPath.length);

    var model = evaluateEmbedded("root-model", "x-ceed/view-model");
    var data = evaluateEmbedded("root-data", "x-ceed/view-data");

    var ViewComponent = viewService.getViewComponent(model.name, model);

    React.render( React.createElement(ViewComponent, data), root);
});

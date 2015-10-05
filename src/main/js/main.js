var bulk = require("bulk-require");
var componentService = require("./service/component");

var Alert = require("./ui/Alert");

var componentsMap = bulk(__dirname, ["components/**/*.json", "components/**/*.js"]);
componentService.registerBulk(componentsMap);

var domready = require("domready");
domready(function ()
{
    var contextPath = document.body.dataset.contextPath;

    var root = document.getElementById("root");

    window.contextPath = contextPath;

    var path = location.pathname.substring(contextPath.length);

    if (path.startsWith("/editor"))
    {
        if (process.env.NODE_ENV !== "production")
        {
            require("./editor")();
        }
        else
        {
            React.render(Alert, root);
        }
    }

});

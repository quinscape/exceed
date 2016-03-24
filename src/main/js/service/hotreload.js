var sys = require("../sys");

var pollingEnabled = false;

const ACTIVITY_TIMEOUT = 60000;

var ajax = require("./ajax");
var viewService = require("./view");

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

if (process.env.NODE_ENV !== "production")
{
    var pollChanges = function ()
    {
        if (!pollingEnabled || polling)
        {
            return;
        }

        polling = true;
        ajax({
            url: sys.contextPath + "/reload/" + sys.appName
        }).then(function (model)
        {
            polling = false;
            //alert("CHANGE" + JSON.stringify(model));

            if (model._type === "view.View")
            {
                viewService.fetch()
                    .then(viewService.render)
                    .then(pollChanges)
                    .catch(function (e)
                    {
                        console.error(e);
                    });
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
                elem.setAttribute("src", timestampURL(sys.contextPath + "/res/" + sys.appName + "/js/main.js"));

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

    var activity = function (ev)
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

module.exports = {
    enablePolling: function ()
    {
        if (process.env.NODE_ENV !== "production")
        {
            pollingEnabled = true;

            document.body.addEventListener("keydown", activity, true);
            document.body.addEventListener("mousemove", activity, true);

            pollChanges();
        }
    }
};

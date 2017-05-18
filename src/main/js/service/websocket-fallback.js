import sys from "../sys";

var pollingEnabled = false;

const ACTIVITY_TIMEOUT = 60000;

import ajax from "./ajax"

// are we currently waiting for a poll response?
var polling = false;
var timeout = false;
var lastActivity = 0;

if (process.env.USE_EDITOR)
{
    var pollChanges = function ()
    {
        if (!pollingEnabled || polling)
        {
            return;
        }

        polling = true;
        ajax({
            url: sys.contextPath + "/wsfallback/" + sys.appName
        }).then(function (model)
        {
            polling = false;
            //alert("CHANGE" + JSON.stringify(model));

            if (model._type === "xcd.view.View")
            {
                fetchView().then(function (data)
                    {
                        return render.render(
                            data.viewModel,
                            data.viewData.data
                        );
                    })
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
//                console.log("CodeChange");

                var elem = document.createElement("script");
                elem.setAttribute("src", timestampURL(sys.contextPath + "/res/" + sys.appName + "/js/main.js"));

                var head = document.getElementsByTagName("head")[0];
                head.appendChild(elem);

            }
            else if (model._type === "change.StyleChange")
            {
                var link = document.getElementById("application-styles");
                link.href = timestampURL(link.href);

//                console.log("StyleChange")
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

export default {
    enablePolling: function ()
    {
        if (process.env.USE_EDITOR)
        {
            pollingEnabled = true;

            document.body.addEventListener("keydown", activity, true);
            document.body.addEventListener("mousemove", activity, true);

            pollChanges();
        }
    }
}

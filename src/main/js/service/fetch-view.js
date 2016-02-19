var ajax = require("./ajax");

/**
 * Refetches the current view from the server via ajax.
 *
 * @param model     Model version to preview, if not given, fetchView will just re-GET the view via AJAX
 * @returns {Promise}
 */
module.exports = function(model)
{

    var isPreview = !!model;
    //console.log("fetchView, isPreview = ", isPreview, JSON.stringify(model, null, "  "));

    var opts = {
        contentType: "JSON",
        url: window.location.href
    };

    if (isPreview)
    {
        opts.method =  "POST";
        opts.contentType = "application/json";
        opts.data = model;
        opts.headers = {
            "X-ceed-Preview" : "true"
        }
    }
    else
    {
        opts.method =  "GET";
    }

    return ajax(opts);
};


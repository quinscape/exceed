var uri = require("../util/uri");
var viewService = require("./view");
var sys = require("../sys");

var assign = require("object.assign").getPolyfill();

var RTView = require("./runtime-view-api");

function renderURI(locInfo, transition)
{
    var params = assign({}, locInfo.params);
    params._trans = transition;

    return uri( "/app/" + sys.appName + locInfo.routingTemplate, params)
}

module.exports = {
    transition: function(name, data)
    {
        var locInfo = viewService.getRuntimeInfo().location;
        return (
            viewService.navigateTo( renderURI(locInfo, name),
                data || {},
                function (data)
                {
                    console.log(data);
                    var locInfo = data.viewData._exceed.location;
                    return renderURI(locInfo);
                }
            )
        );
    },
    list: RTView.prototype.scopedList,
    object: RTView.prototype.scopedObject,
    property: RTView.prototype.scopedProperty
};

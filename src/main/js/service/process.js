var uri = require("../util/uri");
var viewService = require("./view");
var sys = require("../sys");

var RTView = require("./runtime-view-api");

module.exports = {
    transition: function(name)
    {
        var locInfo = viewService.getRuntimeInfo().location;
        return (
            viewService.navigateTo(
                uri( "/app/" + sys.appName + locInfo.routingTemplate, {
                    stateId:  locInfo.params.stateId,
                    _trans:  name
                }),
                true
            )
        );
    },
    list: RTView.prototype.scopedList,
    object: RTView.prototype.scopedObject,
    property: RTView.prototype.scopedProperty
};

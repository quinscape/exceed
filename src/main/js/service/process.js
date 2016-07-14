var uri = require("../util/uri");
var viewService = require("./view");
var Scope = require("./scope");
var sys = require("../sys");

var assign = require("object.assign").getPolyfill();

var RTView = require("./runtime-view-api");

function renderURI(locInfo, transition)
{
    var params = {
        stateId: locInfo.params.stateId,
        _trans : transition
    };

    return uri( "/app/" + sys.appName + locInfo.routingTemplate, params)
}

module.exports = {
    transition: function(name, data)
    {
        var runtimeInfo = viewService.getRuntimeInfo();
        var locInfo = runtimeInfo.location;
        var scopeInfo = runtimeInfo.scopeInfo;

        var names = scopeInfo.queryRefs.concat(scopeInfo.transitionRefs[name].viewScopeRefs);

        return (
            viewService.navigateTo( renderURI(locInfo, name),
                {
                    objectContext: data,
                    contextUpdate: Scope.getScopeUpdate(names)
                },
                function (data)
                {
//                    console.log(data);
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

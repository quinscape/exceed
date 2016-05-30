var actionService = require("../service/action");

var DataList = require("../util/data-list");

module.exports = function (model)
{
    if (!model)
    {
        throw new Error("No model");
    }

    if (!model.object)
    {
        throw new Error("No data");
    }

    var data =  model.object;

    if (data instanceof DataList.Cursor)
    {
        model.object = data.getDomainObject(model.type);
    }
    else if (data._type)
    {
        model.object = data;
    }
    else
    {
        throw new Error("Cannot store unknown object" + model)
    }

    //console.log("STORE", model);

    return actionService.execute(model, true);
};

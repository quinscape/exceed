var actionService = require("../service/action");

var DataList = require("../util/data-list");

module.exports = function (model)
{
    if (!model)
    {
        throw new Error("No model");
    }

    if (!model.data)
    {
        throw new Error("No data");
    }

    var data =  model.data;

    if (data instanceof DataList.Cursor)
    {
        model.data = data.getDomainObject(model.type);
        model.list = null;
    }
    else if (data instanceof DataList)
    {
        console.log("STORE LIST");
        model.data = null;
        model.list = data.dataList.getRaw();
    }
    else if (data._type)
    {
        console.log("STORE DOMAIN OBJECT");
        model.list = null;
    }
    else
    {
        throw new Error("Cannot store unknown object" + model)
    }

    console.log("STORE", model);

    return actionService.execute(model, true);
};

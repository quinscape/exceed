import DataCursor from "../domain/cursor";
const actionService = require("../service/action");

module.exports = function (model)
{
//    console.log("STORE", model);

    if (!model)
    {
        throw new Error("No model");
    }

    if (!model.object)
    {
        throw new Error("No data");
    }

    const data = model.object;

    if (data instanceof DataCursor)
    {
        model.object = data.getDomainObject(model.type);
    }
    else if (data._type)
    {
        model.object = data;
    }
    else
    {
        throw new Error("Cannot store unknown object" + model);
    }

    return actionService.execute(model, true);
};

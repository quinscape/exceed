import DataCursor from "../domain/cursor";
import actionService from "../service/action"

export default function (model)
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
        model.object = data.extractObjects();
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
}

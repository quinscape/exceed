import assign from "object-assign"
import omit from "./omit"

function cleanupComponent(component)
{
    if (!component)
    {
        return null;
    }

    const newComponent = omit(component, "exprs");

    const kids = component.kids;
    if (kids)
    {
        const newKids = new Array(kids.length);
        for (let i = 0; i < kids.length; i++)
        {
            newKids[i] = cleanupComponent(kids[i]);
        }
        newComponent.kids = newKids;
    }


    return newComponent;
}


/**
 * Converts a view model from the interal format to the external format.
 *
 * @param model
 */
export default function(model)
{
    const newModel = omit(model, "titleExpr");
    newModel.synthetic = false;

    const content = newModel.content;
    let newContent = {};

    for (let name in content)
    {
        if (content.hasOwnProperty(name))
        {
            newContent[name] = cleanupComponent(content[name]);
        }
    }

    newModel.content = newContent;

    //console.log("cleaned up", newModel);

    return newModel;
}

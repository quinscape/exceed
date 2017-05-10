export const PROCESS_TYPE = "xcd.process.Process";
export const VIEW_TYPE = "xcd.view.View";

const componentService = require("../service/component");

function componentHasInjection(component)
{
    const descriptor = componentService.getComponents()[component.name];
    if (descriptor && (descriptor.dataProvider || descriptor.queries))
    {
        return true;
    }

    const kids = component.kids;
    if (kids)
    {
        for (let i = 0; i < kids.length; i++)
        {
            const kid = kids[i];

            if (componentHasInjection(kid))
            {
                return true;
            }
        }
    }
    return false;
}

export function isProcess(model)
{
    return model && model.type === PROCESS_TYPE
}

export function isView(model)
{
    return model && model.type === VIEW_TYPE;
}

export function hasInjections(viewModel)
{
    const content = viewModel.content;

    for (let name in content)
    {
        if (content.hasOwnProperty(name))
        {
            let c = content[name];

            if (componentHasInjection(c))
            {
                return true;
            }
        }
    }
    return false;
}


export function matchLocationRule(rule, path)
{
    const matches = path.indexOf(rule.prefix) === 0 && (!rule.infix || path.indexOf(rule.infix, rule.prefix.length));
    return matches && rule.type;
}


export function walk(viewModel, consumer)
{
    if (typeof consumer !== "function")
    {
        throw new Error(consumer + " is no function");
    }


    const content = viewModel.content;

    for (let name in content)
    {
        if (content.hasOwnProperty(name))
        {
            walkRecursive(content[name], consumer);
        }
    }
}

function walkRecursive(component, consumer)
{
    consumer(component);

    const { kids } = component;
    if (kids)
    {
        for (let i = 0; i < kids.length; i++)
        {
            walkRecursive(kids[i], consumer);
        }
    }
}

export function findComponents(viewModel, predicate)
{
    const { content } = viewModel;

    const components = [];
    if (content)
    {

        for (let name in content)
        {
            if (content.hasOwnProperty(name))
            {
                findRecursive(components, content[name], predicate)
            }
        }
    }
    else
    {
        findRecursive(components, viewModel, predicate);
    }
    return components;
}

export function findRecursive(array, component, predicate)
{
    if (predicate(component))
    {
        array.push(component);
    }

    const { kids } = component;
    if (kids)
    {
        for (let obj in kids)
        {
            if (kids.hasOwnProperty(obj))
            {
                findRecursive(array, kids[obj], predicate);
            }
        }
    }
    return true;
}


export function locateComponent(viewModel, chain)
{
    let current = viewModel.content[chain[0]];
    const length = chain.length;
    const last = length - 1;
    for (let i = 1; i < length; i++)
    {
        current = current.kids[chain[i]];

        if (!current && i < last)
        {
            throw new Error("Cannot locate component " + chain + " in " + viewModel.name)
        }
    }

    return current;
}

export function findParent(componentModel, predicate)
{
    let current = componentModel.parent;
    while (current && !predicate(current))
    {
        current = current.parent;
    }
    return current;
}

export function describe(componentModel)
{
    let  s =  "<" + componentModel.name;

    const { attrs } = componentModel;
    if (attrs)
    {
        for (let name in attrs)
        {
            if (attrs.hasOwnProperty(name))
            {
                s += " " + name + "=\"" +attrs[name] + "\"";
            }
        }
    }

    return s + ">"
}

export function isFormComponent(component)
{
    return component && component.name === "Form";
}

import React from "react"
import cx from "classnames"

function findByName(kids, name)
{
    for (let i = 0; i < kids.length; i++)
    {
        const kid = kids[i];
        if (kid.name === name)
        {
            return kid;
        }
    }
    return null;
}

function isDirectory(entry)
{
    return entry.kids.length !== 0;
}

function sortLocations(a,b)
{
    const cmp = (isDirectory(a) ? 0 : 1) - (isDirectory(b) ? 0 : 1);
    if (cmp !== 0)
    {
        return cmp;
    }

    return a.name.localeCompare(b.name);
}

function sortRecursive(entry)
{

    const {kids} = entry;

    let count;
    if (kids.length !== 0)
    {
        kids.sort(sortLocations);
        entry.type = "folder";
        count = 0;
    }
    else
    {
        count = 1;
    }

    //console.log("KIDS", kids.map(kid => kid.name));

    const newKids = [];
    for (let i = 0; i < kids.length; i++)
    {
        const kid = kids[i];
        const kidCount = sortRecursive(kid);
        if (kidCount > 0)
        {
           newKids.push(kid);
           count += kidCount;
        }

    }

    entry.kids = newKids;
    entry.childCount = count;

    return count;
}

const FOLDER_OPEN_ICON = "folder-open";

const MODEL_NAME = {
    "PropertyTypeModel" : "MyPropertyType",
    "EnumType" : "MyEnum",
    "DomainRule" : "DomainSpecificRule",
    "QueryTypeModel" : "QTypeA",
    "StateMachine" : "MyStateMachine",
    "DomainTypeModel" : "DomainTypeA",
    "LayoutModel" : "Layout",
    "Process" : "process-a",
    "View" : "home"
};

const ICON_NAME = {
    "PropertyTypeModel" : "asterisk",
    "EnumType" : "option-vertical",
    "DomainRule" : "",
    "QueryTypeModel" : "",
    "StateMachine" : "ok-circle",
    "DomainTypeModel" : "",
    "LayoutModel" : "picture",
    "Process" : "cog",
    "View" : "picture",
    "ApplicationConfig" : "wrench",
    "RoutingTable" : "road"
};


export function makeTree(locations, filter)
{
    let tree = {
        name : "root",
        kids: []
    };

    if (!filter)
    {
        tree.kids.push({
            name : "resources",
            type : "dir",
            icon: FOLDER_OPEN_ICON,
            kids: [
                {
                    name : "css",
                    type : "dir",
                    icon: FOLDER_OPEN_ICON,
                    kids: [
                        {
                            name : "mybootstrap-theme.min.css",
                            type : "asset",
                            kids: [],
                            icon: "picture"
                        }
                    ]
                },
                {
                    name : "image",
                    type : "dir",
                    icon: FOLDER_OPEN_ICON,
                    kids: [
                        {
                            name : "mylogo.jpg",
                            type : "asset",
                            kids: [],
                            icon: "picture"
                        }
                    ]
                }
            ]

        });
    }

    for (let i = 0; i < locations.length; i++)
    {
        const { prefix, suffix, type } = locations[i];

        const pos = type.lastIndexOf(".");
        const shortType = type.substring(pos + 1);
        const joined =
            suffix ?
            prefix + "process-a" + suffix :
            shortType === "Process" ?
                prefix + "/process-a" :
                prefix;
        const path = joined.indexOf(".json") < 0 ? joined + "/" + (suffix ? "process-view" : MODEL_NAME[shortType]) + ".json" : joined;

        if (filter && !filter.test(path))
        {
            continue;
        }

        const parts = path.split("/").filter(s => !!s.length);

        let current = tree;
        for (let j = 0; j < parts.length; j++)
        {
            let kid = findByName(current.kids, parts[j]);

            if (!kid)
            {
                kid = {
                    name: parts[j],
                    type: shortType,
                    path: path,
                    modelType: type,
                    kids: [],
                    childCount: -1,
                    icon: FOLDER_OPEN_ICON
                };

                current.kids.push(kid);
            }

            current = kid;
        }

        current.icon = ICON_NAME[shortType];

    }

    sortRecursive(tree);

    return tree;
}

export default function ExtensionDir(props)
{
    const { name, type, kids, modelType, last, icon } = props;

    const lastKid = kids && kids[kids.length-1];

    const isAsset = type === "asset";
    const isFolder = type === "folder";
    const haveLink = !isAsset && !isFolder;

    return (
        <li className={ cx( last && "last" )} >
            {
                React.createElement(
                    haveLink ? "a" : "span",
                    {
                        className: "type-" + type,
                        href: haveLink ? "./model-reference.html#" + modelType : null
                    },
                    !!icon && <span className={ "glyphicon glyphicon-" + icon }/>,
                    " " + name,
                    kids &&
                    <ul>
                        {kids.map(kid => <ExtensionDir key={kid.name} {...kid} last={kid === lastKid}/>)}
                    </ul>
                )
            }
        </li>
    )
}

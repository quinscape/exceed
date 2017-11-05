import React from "react"
import ExtensionLocation, { makeTree } from "./ExtensionLocation"

function ExtensionStructure(props)
{
    const { locations, filter, extra } = props;

    const tree = makeTree(locations, filter, extra);

    const last = tree.kids[tree.kids.length-1];

    return(
        <ul className="extension-struct">
            {
                tree.kids && tree.kids.map( kid => <ExtensionLocation key={ kid.name } {...kid} last={ kid === last} filter={ filter }/> )
            }
        </ul>
    );
}

export default ExtensionStructure;

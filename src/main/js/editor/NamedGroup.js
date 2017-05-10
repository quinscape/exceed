import React from "react"
import cx from "classnames"

import Memoizer from "../util/memoizer"
import Icon from "../ui/Icon"

import keys from "../util/keys"

import ModelLink from "./ModelLink"

function sortEntries(a,b)
{
    return a.name.localeCompare(b.name);
}

const getSortedModels = Memoizer( (map, type, icon) => {

    return keys(map).map(name => ({
        name,
        type,
        icon
    })).sort(sortEntries);

});

const getSortedModelsAndSubs = Memoizer( (map, type, icon, subType, subItems, subIcon) => {

    console.log({type,subType,subItems});

    let entries = keys(map).map(name => ({
        name,
        type,
        icon
    }));

    if (subType)
    {
        entries = entries.concat(keys(subItems).map(name => ({
            name: name,
            type: subType,
            icon: subIcon
        })));
    }

    return entries.sort(sortEntries);
});

class NamedGroup extends React.Component {

    state = {
        open: true
    };

    toggle = ev => {

        this.setState({
            open: !this.state.open
        });

        ev.preventDefault();
    };

    render()
    {
        const { map, type, currentLocation, filter, icon, subIcon, subType, subItems, children } = this.props;
        const { open } = this.state;

        let entries =  subType ? getSortedModelsAndSubs(map, type, icon, subType, subItems, subIcon) : getSortedModels(map, type, icon);

        if (filter)
        {
            entries = entries.filter(entry => entry.name.toLowerCase().indexOf(filter) >= 0);
        }

        if (!entries.length)
        {
            return false;
        }

        return (
            <div className="group">
                <div className="header">
                    <h5>
                        <a href="#toggle" onClick={ this.toggle }>
                        <Icon className={ open ? "glyphicon-chevron-down" : "glyphicon-chevron-right" }/>
                        </a>
                        { " " }
                        { children }
                    </h5>
                </div>
                {
                    open && entries.map( ({name, type, icon}) =>
                        <ModelLink key={ name } type={ type } name={ name } currentLocation={ currentLocation }>

                            { icon && <Icon className={ "glyphicon-" + icon } /> }
                            { icon && " " }
                            { name }
                        </ModelLink>
                    )
                }
            </div>
        )
    }
}

export default NamedGroup

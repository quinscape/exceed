import i18n from "../../src/main/js/service/i18n"
import Memoizer from "../../src/main/js/util/memoizer";
import endsWith from "../../src/main/js/util/endsWith";
import Icon from "../../src/main/js/ui/Icon";
import cx from "classnames";

import React from "react"

function sortByName(a,b)
{
    return a.name.localeCompare(b.name);
}

const getSortedProps = Memoizer(doc => {
    const propDocs = doc.propertyDocs;
    
    return {
        norm: propDocs
            .filter(propDoc => propDoc.subTypeDocs.length <= 0 || !propDoc.subTypeDocs.some(type => type !== doc.type))
            .sort(sortByName),

        sub: propDocs
            .filter(propDoc => propDoc.subTypeDocs.length > 0 && propDoc.subTypeDocs.some( type => type !== doc.type ))
            .sort(sortByName)
    }
});

function Description({ value, prefix = "", className })
{
    return (
        value && <div className={ cx("description", className) } dangerouslySetInnerHTML={{__html: prefix + value}} />
    )
}

function Section(type, docs)
{
    const doc = docs[type];

    if (!doc)
    {
        throw new Error("No doc for type: " + type);
    }

    const propDocs = getSortedProps(doc);

    return (
        <div key={ type } id={ type }>
            {
                type === "xcd.component.ComponentPackageDescriptor" &&
                <div>
                    <br/>
                    <h1>Components</h1>
                    <hr/>
                </div>
            }
            {
                doc.locationDescription ? (
                    [
                        <h2 key="1">
                            { "Type: " + doc.type }

                            <div className="byline">
                                Location: { doc.locationDescription }
                            </div>
                        </h2>,
                        <Description  key="2" value={ doc.classDescription }/>
                    ]
                ) : (
                    <Description prefix={ "<strong>" + doc.type + ": " + "</strong>" } value={ doc.classDescription }/>
                ) 
            }

            <table className="table table-bordered table-striped">
                <thead>
                <tr>
                    <th width="20%">{ i18n("Attribute") }</th>
                    <th>{ i18n("type") }</th>
                    <th>{ i18n("Description") }</th>
                </tr>
                </thead>
                <tbody>
                {
                    propDocs.norm.map( propDoc =>
                        <tr key={ propDoc.name } >
                            <td className="prop-name">
                                { propDoc.name }
                            </td>
                            <td className="type-desc" >
                                { propDoc.typeDescription }
                            </td>
                            <td>
                                <Description value={ propDoc.propertyDescription }/>
                            </td>
                        </tr>
                    )
                }
                {
                    propDocs.sub.map( propDoc => {
                        return [
                            <tr key={ propDoc.name } >
                                <td className="prop-name">
                                    { propDoc.name }
                                </td>
                                <td className="type-desc" >
                                    { propDoc.typeDescription }
                                </td>
                                <td>
                                    <Description value={ propDoc.propertyDescription }/>
                                </td>
                            </tr>,
                            <tr key={ propDoc.name + "sub" } >
                                <td colSpan={3}>
                                    {
                                        propDoc.subTypeDocs.map(subType => Section(subType, docs))
                                    }
                                </td>
                            </tr>
                        ]
                    })
                }
                </tbody>
            </table>
        </div>
    )
}

function Navigation(props)
{
    const { modelLocations } = props;

    return (
        <ul id="top" className="model-locations">
            {
                modelLocations.map( (loc,idx) =>
                    <li key={ idx }>
                        <a className="btn btn-link" href={ "#" + loc.type}>
                            <Icon className="glyphicon-folder-open text-info" />
                            <span className="location">
                                    {
                                        loc.suffix ?
                                            loc.prefix + "*" + loc.suffix :
                                            loc.prefix + (endsWith(loc.prefix, ".json") ? "" : "*")
                                    }
                                    </span>
                        </a>
                    </li>
                )
            }
        </ul>
    )
}


class ModelDocs extends React.Component {

    static propTypes = {
        locations: React.PropTypes.array.isRequired,
        modelDocs: React.PropTypes.object.isRequired
    };

    render()
    {
        const { locations, modelDocs } = this.props;

        const docs = modelDocs.docs;

        return (
            <div className="model-docs">
                <h1>Model Locations</h1>
                <Navigation modelLocations={ locations }/>

                <h1>Application Models</h1>

                <p>
                    Each section listed here corresponds to a top level model equivalent to a JSON file at the relative
                    locations listed above. The type names of both top level models and nested models correspond to the
                    Java class model hierarchy. The prefix "xcd" gets replaced with the base package of the Java class model
                    hierarchy ( de.quinscape.exceed.model ). So "xcd.view.View" maps to "de.quinscape.exceed.model.view.View"
                    etc.
                </p>

                <hr/>

                {
                    modelDocs.topLevelTypes.map(type => Section(type, docs) )
                }

            </div>
        )
    }
}

export default ModelDocs

const i18n = require("../../src/main/js/service/i18n");
import React from "react";
import Annotate from "../../src/main/js/ui/Annotate";
import { Rule } from "../../src/main/js/ui/Annotate";

import Icon from "../../src/main/js/ui/Icon";

function sortPropTypesByName(a,b)
{
    return a.name.localeCompare(b.name);
}

function preparePropTypes(propTypes, context)
{
    if (!propTypes)
    {
        return false;
    }

    const out = [];
    for (let name in propTypes)
    {
        if (propTypes.hasOwnProperty(name))
        {
            const propType = propTypes[name];
            propType.name = name;
            out.push(propType);
        }
    }

    return (
        out
            .filter(propType => !!propType.context === context)
            .sort(sortPropTypesByName)
    );
}

function ContextProps(props)
{
    const { contextProps } = props;

    if (!contextProps)
    {
        return false;
    }

    return (
        <p>
            <strong>Receives</strong>:
            {
                contextProps.map(contextProp => {
                    const { name, context, contextType } = contextProp;
                    return (
                        <button key={ name } disabled={true} className="btn btn-sm" title={  context === true ? "context" : context }>
                            { name + "(" + contextType + ")" }
                        </button>
                    );
                })
            }
        </p>
    )
}

function RichDescription(props)
{
    const { value } = props;

    if (!value)
    {
        return false;
    }

    return (
        <p
            dangerouslySetInnerHTML={
                {
                    __html: value
                }
            }
        />
    );
}

function AnnotatedExpression(props)
{
    const { value } = props;

    return (
        <Annotate value={ value }>
            <Rule regexp="Class\('(.*?)'\)">
                {
                    (value, cls) =>
                        <span>
                            Class(<a
                                href={ "class-index.html#Class-" + cls }
                            >
                                '{ cls }'
                            </a>)
                        </span>
                }
            </Rule>
            <Rule regexp="component\('(.*?)'\)">
                {
                    (value, name) =>
                        <span>
                            component(<a
                            href={ "component.html#Component-" + name }
                        >
                            '{ name }'
                            </a>)
                        </span>
                }
            </Rule>
        </Annotate>
    )
}


function ComponentDoc(props)
{
    const { name, descriptor } = props;

    const classes = descriptor.classes;

    const propTypes = preparePropTypes(descriptor.propTypes, false);
    const contextProps = preparePropTypes(descriptor.propTypes, true);

    return (
        <div id={ "Component-" + name }>
            <h3>{ name }</h3>
            <RichDescription value= { descriptor.description }/>
            {
                propTypes &&
                <table className="table table-responsive table-bordered table-striped">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>
                    {
                        propTypes.map(propType =>
                            <tr key={ propType.name }>
                                <td>
                                    { propType.name }
                                </td>
                                <td>
                                    { propType.type }
                                </td>
                                <td>
                                    <RichDescription value={ propType.description }/>
                                </td>
                            </tr>
                        )
                    }
                    </tbody>
                </table>
            }

            {
                descriptor.providesContext &&
                <p>
                    <strong>Provides</strong>: "{ descriptor.providesContext }"
                </p>
            }

            <ContextProps contextProps={ contextProps }/>

            {
                descriptor.childRule &&
                <p>
                    <strong>childRule</strong> : <AnnotatedExpression value={ descriptor.childRule }/>
                </p>
            }

            {
                descriptor.parentRule &&
                <p>
                    <strong>parentRule</strong> : <AnnotatedExpression value={ descriptor.parentRule }/>
                </p>
            }

            {
                classes &&
                <div className="classes btn-toolbar">
                    {
                        classes.sort().map(cls =>
                            <a key={ cls } className="btn btn-sm" href={ "class-index.html#index-" + cls }>
                                <Icon className="glyphicon-tag text-info"/>
                                { " " + cls }
                            </a>
                        )
                    }
                </div>
            }
            <hr/>
        </div>
    )
}

class ComponentDocs extends React.Component {

    render()
    {
        const { names, descriptors } = this.props.data;

        return (
            <div className="component-docs">
                <h1>Component Docs</h1>
                <p>
                    Documentation for the exceed components currently available. (Auto-generated from the components.json
                    package descriptors).
                </p>
                <h2>Overview</h2>
                <ul className="no-bullet">
                {
                    names.map(name =>
                        <li key={ name }>
                            <a className="btn btn-link" href={ "#Component-" + name }>
                                <Icon className="glyphicon-th-large text-info"/>
                                { " " + name }
                            </a>
                        </li>
                    )
                }
                </ul>
                {
                    names.map(name =>
                        <ComponentDoc key={ name } name={ name } descriptor={ descriptors[name] } />
                    )
                }
            </div>
        )
    }
}

export default ComponentDocs

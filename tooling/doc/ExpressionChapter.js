import React from "react"

import describeProp from "../../src/main/js/util/describe-property"
import Markdown from "./Markdown"

function describeProperty(prop)
{
    if (!prop)
    {
        return "Any";
    }
    return describeProp(prop);
}

function paramList(def)
{
    const { parameterModels } = def;

    if (!parameterModels || !parameterModels.length)
    {
        return "";
    }
    let s= "";

    const len = parameterModels.length;

    for (let i = 0; i < len; i++)
    {
        if (i > 0)
            s+=", ";

        const prop = parameterModels[i];

        s += describeProperty(prop);

        if (prop.name)
        {
            s += " " + prop.name;
        }

    }

    if (def.varArgs)
    {
        s += "...";
    }

    return s;
}

class ExpressionChapter extends React.Component {

    render()
    {
        const { name, definitions, noDefTypes, intro, expressionType } = this.props;

        return (
            <div>
                <h1> { name } </h1>
                {
                    intro && <Markdown data={ intro }/>
                        
                }
                {
                        definitions.map((def,idx) => {

                        if (def.returnType)
                        {
                            return (
                                <div key={ idx }>
                                    <h2>
                                        { def.name.indexOf(".") >= 0 ? def.name : "Function " + def.name }
                                        <br/>

                                        { !noDefTypes && <small>Type: { def.definitionType }</small> }
                                    </h2>
                                    <Markdown data={
                                        "```" + expressionType + "\n" + describeProperty(def.returnType ) + " " + def.name + "(" + paramList(def)  + ")" + "\n```\n"
                                    }/>
                                    <p dangerouslySetInnerHTML={ { __html: def.description } } />
                                </div>
                            );
                        }
                        else
                        {
                            return (
                                <div key={ idx }>
                                    <h2>Identifier { def.name }</h2>
                                    { "Type: "+ describeProperty(def.propertyType ) }
                                    <p dangerouslySetInnerHTML={ { __html: def.description } } />
                                </div>
                            );

                        }
                    })
                }
            </div>
        )
    }
}

export default ExpressionChapter

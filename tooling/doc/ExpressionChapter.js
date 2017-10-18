import React from "react"

import describeProp from "../../src/main/js/util/describe-property"

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
        const { name, definitions } = this.props;

        return (
            <div>
                <h1> { name } </h1>
                {
                        definitions.map((def,idx) => {

                        if (def.returnType)
                        {
                            return (
                                <div key={ idx }>
                                    <h2>Function { def.name }</h2>
                                    <pre>
                                    { describeProperty(def.returnType ) + " " + def.name + "(" + paramList(def)  + ")" }
                                    </pre>
                                    <p>
                                        { def.description}
                                    </p>

                                </div>
                            );
                        }
                        else
                        {
                            return (
                                <div key={ idx }>
                                    <h2>Identifier { def.name }</h2>
                                    { "Type: "+ describeProperty(def.propertyType ) }
                                    <p>
                                        { def.description}
                                    </p>
                                </div>
                            );

                        }
                    })
                }
                <hr/>
            </div>
        )
    }
}

export default ExpressionChapter

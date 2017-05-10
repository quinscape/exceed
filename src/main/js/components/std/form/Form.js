import React from "react";
import cx from "classnames";
import DataCursor from "../../../domain/cursor"
import DataGraph, { validateDataGraph } from "../../../domain/graph"

import Scope from "../../../service/scope";
const domainService = require("../../../service/domain");
import FormContext from "../../../util/form-context";
import LinkedStateMixin from "react-addons-linked-state-mixin";

import renderWithContext from "../../../util/render-with-context";

function objectToDataGraph(input)
{
    const typeName = input._type;
    const domainType = Scope.objectType(typeName);

    const cols = {};
    const obj = {};
    for (let name in input)
    {
        if (input.hasOwnProperty(name))
        {
            if (name === "_type")
            {
                obj[name] = input[name];
            }
            else
            {
                const qualified = typeName + "." + name;
                obj[qualified] = input[name];
                cols[qualified] = {
                    type: typeName,
                    name: name
                };
            }
        }
    }

    return {
        type : "ARRAY",
        types: {
            [typeName] : domainType
        },
        columns: cols,
        rootObject: [ obj ],
        isMap: false,
        count: 1
    };
}

class Form extends React.Component
{

    static contextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    };

    // childContextTypes: {
    //     formContext: React.PropTypes.instanceOf(FormContext)
    // },

    static propTypes = {
        data: React.PropTypes.object.isRequired,
        horizontal: React.PropTypes.bool,
        labelClass: React.PropTypes.string,
        wrapperClass: React.PropTypes.string,
        path: React.PropTypes.array
    };

    static defaultProps = {
        horizontal: true,
        labelClass: "col-md-2",
        wrapperClass: "col-md-4",
        path: [0]
    };

    cursorFromData(data)
    {
        if (!data)
        {
            throw new Error("No data");
        }

        if (validateDataGraph(data))
        {
            return new DataCursor(domainService.getDomainTypes(), data, this.props.path);
        }
        else if (data instanceof DataCursor)
        {
            return data;
        }
        else if (data._type)
        {
                return new DataCursor(domainService.getDomainTypes(), objectToDataGraph(data), this.props.path);
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    }

    onSubmit = ev => {

        const primaryButtons = document.querySelectorAll(".btn-primary");
        if (primaryButtons.length && !primaryButtons[0].disabled)
        {
            primaryButtons[0].focus();
        }
        else
        {
            const tmp = document.createElement("input");
            document.body.appendChild(tmp);
            tmp.focus();
            document.body.removeChild(tmp);
        }

        ev.preventDefault();
    };

    // onChange: function (newGraph, path)
    // {
    //     //console.log("onChange", JSON.stringify(newGraph), path);
    //
    //     this.setState({
    //         dataGraph: newGraph
    //     });
    // },

    render()
    {
        const ctx = this.context.formContext;
        const cursor = this.cursorFromData(this.props.data);
        return (
            <form className={ cx( ctx && ctx.horizontal ? "form-horizontal" : "form") } onSubmit={ this.onSubmit }>
                { renderWithContext(this.props.children, cursor) }
            </form>
        );
    }
}

export default Form;


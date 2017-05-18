import React from "react";
import DataCursor from "../../../domain/cursor";
import { validateDataGraph } from "../../../domain/graph";
import domainService from "../../../service/domain";
import FormElement from "./FormElement";


const SelectField = FormElement(class {

    static displayName = "SelectField";

    getInputField()
    {
        return this._input;
    }

    options()
    {
        const input = this.props.data;
        let array;
        if (validateDataGraph(input))
        {
            //console.log("RENDER DATA LIST OPTIONS");
            array = new DataCursor(
                    domainService.getDomainTypes(),
                    input,
                    []
                )
                .get();
            
            const display = this.props.display;
            const value = this.props.value;

            return ( array.map(
                    display ?
                        o => <option key={ o[value] } value={ o[value] }>{ o[display] }</option> :
                        o => <option key={ o[value] } value={ o[value] }>{ o[value] }</option>
                )
            );
        }
        else if (input instanceof DataCursor)
        {
            //console.log("GET CURSOR");
            array = input.value;

        }
        else if (input && input.length && input[0] !== undefined)
        {
            //console.log("GET ARRAY");
            array = input;
        }

        if (array)
        {
            //console.log("RENDER ARRAY");

            let firstElement = array[0];
            if (firstElement && typeof firstElement === "object")
            {
                return (
                    array.map(
                        (o, idx) => <option key={ idx } value={ o.value }>{ o.display }</option>
                    )
                );
            }
            else
            {
                return (
                    array.map(
                        (o, idx) => <option key={ idx } value={ o }>{ o }</option>
                    )
                );
            }
        }

        throw new Error("Unhandled input: " + input);
    }

    render()
    {
        const value = this.props.valueLink.value;

        //console.log("SELECT VALUE", value);

        return (
            <select
                id={ this.props.id }
                ref={ elem => this._input = elem}
                className="form-control"
                value ={ value }
                disabled={ this.props.disabled }
                onChange={ (ev) => this.props.onChange( ev.target.value) }
            >
                { this.options(value) }
            </select>
        )
    }
});

export default SelectField


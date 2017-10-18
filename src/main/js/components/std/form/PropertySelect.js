/**
 * Internal enum select component used by Field
 */
import domainService from "../../../service/domain";
import converter from "../../../service/property-converter";
import i18n from "../../../service/i18n";
import cx from "classnames";
import React from "react";

/**
 * Generic select component to select values for a property type.
 */
export class PropertySelect extends React.Component
{
    getInputField ()
    {
        return this._input;
    }

    onChange = ev => this.props.onChange(ev.target.value);

    render ()
    {
        const { value, id, disabled, className, supplier } = this.props;

        return (
            <select
                id={ id }
                ref={ elem => this._input = elem}
                className={ cx("form-control", className) }
                value={ value }
                disabled={ disabled }
                onChange={ this.onChange }
            >
                {
                    supplier(this.props)
                }
            </select>
        );
    }
}


export default PropertySelect

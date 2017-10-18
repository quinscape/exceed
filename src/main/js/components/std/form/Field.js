import React from "react";
import FormElement from "./FormElement";
import Checkbox from "./Checkbox";
import CalendarField from "./CalendarField";
import PropertySelect from "./PropertySelect";
import i18n from "../../../service/i18n";

const domainService = require("../../../service/domain");
import PropTypes from 'prop-types'

function supplyEnumValues({ propertyType })
{
    const enumModel = domainService.getEnumType(propertyType.typeParam);
    return enumModel.values.map(
        (value, idx) =>
            <option key={ idx } value={ value }>
                { i18n(enumModel.name + " " + value) }
            </option>
    )
}

function supplyStateMachineValues({ value, propertyType })
{
    const stateMachineModel = domainService.getStateMachine(propertyType.typeParam);
    const states = stateMachineModel.states[value];

    const options =  states.map(
        (value, idx) =>
            <option key={ idx } value={ value }>
                { i18n( stateMachineModel.name + " " + value ) }
            </option>
    );

    options.unshift(
        <option key={ -1 } value={ value }>
            { i18n( stateMachineModel.name + " " + value ) }
        </option>
    )
}

const Field = FormElement(class Field extends React.PureComponent
{
    getInputField()
    {
        const input = this._input;
        if (typeof input.appendChild === "function")
        {
            return input
        }

        return input.getInputField();
    }

    onChange = ev => this.props.onChange(ev.target.value, false);

    onBlur = ev => {

        if (!this.props.propagate)
        {
            this.props.onChange(this.getInputField().value, true);
        }
    };

    render()
    {
        const { propertyType } = this.props;

        const typeName = propertyType.type;
        if (typeName === "Date" || typeName === "Timestamp")
        {
            return (
                <CalendarField
                    ref={ component => this._input = component}
                    {... this.props }/>
            );
        }

        if (typeName === "Enum")
        {
            return (
                <PropertySelect
                    ref={ component => this._input = component }
                    supplier={ supplyEnumValues }
                    {... this.props }
                />
            );
        }

        if (typeName === "State")
        {
            return (
                <PropertySelect
                    ref={ component => this._input = component }
                    supplier={ supplyStateMachineValues }
                    {... this.props }
                />
            );
        }


        if (typeName === "Boolean")
        {
            return (
                <Checkbox
                    ref={ component => this._input = component}
                    {... this.props }
                />
            );
        }

        const { value, id, disabled, autoFocus } = this.props;

        return (
            <input
                id={ id }
                ref={ elem => this._input = elem}
                type="text"
                className="form-control"
                value={ value }
                onChange={ this.onChange }
                disabled={ disabled }
                autoFocus={ autoFocus }
                onBlur={ this.onBlur }
            />
        )
    }
}, {
    decorate: pt => pt.type !== "Boolean"
});

export default Field

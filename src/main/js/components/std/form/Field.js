import React from "react";
import FormElement from "./FormElement";
import Checkbox from "./Checkbox";
import CalendarField from "./CalendarField";
import EnumSelect from "./EnumSelect";


const Field = FormElement(class Field extends React.Component
{
    static propTypes = {
        validate: React.PropTypes.func
    };

    getInputField()
    {
        const input = this._input;
        if (typeof input.appendChild === "function")
        {
            return input
        }

        return input.getInputField();
    }

    render()
    {
        const typeName = this.props.propertyType.type;
        if (typeName === "Date" || typeName === "Timestamp")
        {
            return (
                <CalendarField
                    ref={ component => this._input = component}
                    {...this.props}/>
            )
        }

        if (typeName === "Enum")
        {
            return (
                <EnumSelect
                    ref={ component => this._input = component}
                    {...this.props}/>
            )
        }

        if (typeName === "Boolean")
        {
            return (
                <Checkbox
                    ref={ component => this._input = component}
                    {...this.props}/>
            )
        }

        return (
            <input
                id={ this.props.id }
                ref={ elem => this._input = elem}
                className="form-control"
                value={ this.props.valueLink.value }
                onChange={ ev => this.props.valueLink.requestChange(ev.target.value) }
                disabled={ this.props.disabled }
                onBlur={ (ev) => {
                    const value = ev.target.value;
                    //console.log("input CHANGE", value);
                    this.props.onChange( value)
                }}
                autoFocus={ this.props.autoFocus }
            />
        )
    }
});

export default Field

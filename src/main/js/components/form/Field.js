const React = require("react");

const FormElement =  require("./FormElement");
const Checkbox =  require("./Checkbox");

const CalendarField = require("./CalendarField");
const EnumSelect = require("./EnumSelect");

var Field = FormElement(React.createClass({

    propTypes: {
        validate: React.PropTypes.func
    },

    getInputField: function ()
    {
        var input = this._input;
        if (typeof input.appendChild == "function")
        {
            return input
        }

        return input.getInputField();
    },

    render: function ()
    {
        var typeName = this.props.propertyType.type;
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
                valueLink={ this.props.valueLink }
                disabled={ this.props.disabled }
                onBlur={ (ev) => {
                    this.props.onChange( ev.target.value)
                }}
                autoFocus={ this.props.autoFocus }
            />
        )
    }
}));

module.exports = Field;

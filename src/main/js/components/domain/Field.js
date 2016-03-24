var React = require("react");

var FormElement =  require("./FormElement");

var CalendarField = require("./CalendarField");
var EnumSelect = require("./EnumSelect");

var Field = FormElement(React.createClass({

    render: function ()
    {
        var typeName = this.props.propertyType.type;
        if (typeName === "Date" || typeName === "Timestamp")
        {
            return (
                <CalendarField {...this.props}/>
            )
        }

        if (typeName === "Enum")
        {
            return (
                <EnumSelect {...this.props}/>
            )
        }

        return (
            <input
                id={ this.props.id }
                className="form-control"
                valueLink={ this.props.valueLink }
                onBlur={ (ev) => {
                    this.props.onChange( ev.target.value)
                } }
            />
        )
    }
}));

module.exports = Field;

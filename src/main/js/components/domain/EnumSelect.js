var React = require("react");
var cx = require("classnames");

var i18n  = require("../../service/i18n");
var domainService  = require("../../service/domain");

var FormContext = require("./form-context");

/**
 * Internal enum select component used by Field
 */
var EnumSelect = React.createClass({

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    render: function ()
    {
        var enumModel = domainService.getEnum(this.props.propertyType.typeParam);

        return (
            <select
                id={ this.props.id }
                className={ cx("form-control", this.props.className) }
                defaultValue={ this.props.valueLink.value }
                onChange={ (ev) => this.props.onChange(ev.target.value) }
            >
                {
                    enumModel.values.map(
                        (value, idx) =>
                        <option key={ idx } value={ value }>
                            { i18n(enumModel.name + " " + value) }
                        </option>
                    )
                }
            </select>
        );
    }
});

module.exports = EnumSelect;

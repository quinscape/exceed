/**
 * Internal enum select component used by Field
 */
import FormContext from "../../../util/form-context";
var React = require("react");
var cx = require("classnames");

var i18n  = require("../../../service/i18n");
var domainService  = require("../../../service/domain");

var EnumSelect = React.createClass({

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    getInputField: function ()
    {
        return this._input;
    },

    render: function ()
    {
        var enumModel = domainService.getEnum(this.props.propertyType.typeParam);

        return (
            <select
                id={ this.props.id }
                ref={ elem => this._input = elem}
                className={ cx("form-control", this.props.className) }
                value={ this.props.valueLink.value }
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

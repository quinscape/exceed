/**
 * Internal enum select component used by Field
 */
import FormContext from "../../../util/form-context";
import domainService from "../../../service/domain";
import i18n from "../../../service/i18n";
import cx from "classnames";
import React from "react";


class EnumSelect extends React.Component
{

    static contextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    }

    getInputField ()
    {
        return this._input;
    }

    render ()
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
};

export default EnumSelect
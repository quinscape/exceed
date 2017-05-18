/**
 * Checkbox component
 */
import FormContext from "../../../util/form-context";
import FormElement from "./FormElement";
import domainService from "../../../service/domain";
import i18n from "../../../service/i18n";
import cx from "classnames";
import React from "react";


class Checkbox extends React.Component
{

    static contextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    }

    getInputField()
    {
        return this._input;
    }

    render()
    {
        var ctx = this.context.formContext;
        var pt = this.props.propertyType;

        return (
            <div className="checkbox">
                <label>
                    <input type="checkbox"
                           id={ this.props.id }
                           ref={ elem => this._input = elem}
                           checked={ this.props.valueLink.value }
                           onChange={ (ev) => this.props.onChange(!this.props.valueLink.value) }
                    />
                    { this.props.label || i18n(pt.parent + ":" + pt.name) }
                </label>
            </div>
        );
    }
};

export default Checkbox

/**
 * Checkbox component
 */
import i18n from "../../../service/i18n";
import React from "react";

class Checkbox extends React.PureComponent
{
    getInputField()
    {
        return this._input;
    }

    onChange = ev => this.props.onChange(!this.props.value);

    render()
    {
        const pt = this.props.propertyType;
        return (
            <div className="checkbox">
                <label>
                    <input type="checkbox"
                           id={ this.props.id }
                           ref={ elem => this._input = elem}
                           checked={ this.props.value }
                           onChange={ this.onChange }
                    />
                    { this.props.label || i18n(pt.parent + ":" + pt.name) }
                </label>
            </div>
        );
    }
}

export default Checkbox

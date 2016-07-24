var React = require("react");
var cx = require("classnames");

var i18n = require("../../service/i18n");
var domainService = require("../../service/domain");

var FormContext = require("../../util/form-context");

const FormElement =  require("./FormElement");

/**
 * Checkbox component
 */
var Checkbox = React.createClass({

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    getInputField: function ()
    {
        return this._input;
    },

    render: function ()
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
});

module.exports = Checkbox;

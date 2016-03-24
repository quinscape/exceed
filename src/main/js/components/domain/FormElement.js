
var immutableUpdate = require("react-addons-update");

var React = require("react");

var cx = require("classnames");

var i18n = require("../../service/i18n");

var DataList = require("../../util/data-list");

var FormContext = require("./form-context");

var ValueLink = require("../../util/value-link");

var converter = require("../../service/property-converter");

/**
 * FormField is a high order component for bootstrap form fields. It handles all the cursor logic and type analysis and
 * error checking.
 *
 * @param FieldComponent
 * @returns {*}
 */
module.exports = function(FieldComponent)
{
    //console.log("Create FormElement: ", InputComponent.displayName);

    return React.createClass({

        displayName: "FormElem(" + (FieldComponent.displayName + ")" || "Unnamed"),

        contextTypes: {
            formContext: React.PropTypes.instanceOf(FormContext)
        },

        propTypes: {
            context: React.PropTypes.instanceOf(DataList.Cursor)
        },

        getInitialState: function ()
        {
            var ctx = this.context.formContext;

            var value = converter.toUser(this.props.context.value, this.props.context.getPropertyType());

            //console.log("VALUE", value);

            return {
                value : value.value,
                id: ctx.nextId()
            };
        },

        validate: function (value)
        {
            var id = this.state.id;

            //console.log("VALIDATE", value);

            var result = converter.fromUser(value, this.props.context.getPropertyType());
            var isOk = result.ok;

            var ctx = this.context.formContext;

            var haveError = ctx.hasError(id);

            if (!isOk)
            {
                ctx.signalError(id, result.error);
                this.setState({
                    value: value
                });
            }
            else
            {
                if (haveError)
                {
                    ctx.signalError(id, null);
                }

                this.setState({
                    value: result.value
                });
            }

            return result;
        },

        onChange: function (value)
        {
            var result = this.validate(value);

            if (result.ok)
            {
                this.props.context.set(null, result.value);
            }
        },

        shouldComponentUpdate: function (nextProps, nextState)
        {
            return this.state.value !== nextState.value;
        },

        //componentWillReceiveProps: function (nextProps)
        //{
        //    this.setState({
        //        value: nextProps.context.value
        //    });
        //},

        render: function ()
        {

            var cursor = this.props.context;

            var ctx = this.context.formContext;
            var id = this.state.id;

            var errorMessage = ctx.getErrorMessage(id);

            var pt = cursor.getPropertyType();
            var labelElement = (
                <label className={ cx("control-label", ctx.horizontal && ctx.labelClass(this)) } htmlFor={ id }>
                    { this.props.label || i18n(pt.parent + "." + pt.name) }
                </label>
            );

            var fieldComponent = (
                <FieldComponent
                    ref="field"
                    id={ id }
                    valueLink={ new ValueLink(this.state.value, this.validate) }
                    className={ this.props.className }
                    validate={ this.props.validate }
                    propertyType={ pt }
                    onChange={ this.onChange }
                />
            );


            var helpBlock = false;

            if (errorMessage)
            {
                helpBlock = (
                    <p className="text-danger">
                        { errorMessage }
                    </p>
                )
            }

            if (ctx.horizontal)
            {
                fieldComponent = (
                    <div className={ ctx.wrapperClass(this) }>
                        { fieldComponent }
                        { helpBlock }
                    </div>
                )
            }

            return (
                <div className={ cx("form-group", errorMessage && "has-error") }>
                    { labelElement }
                    { fieldComponent }
                    { !ctx.horizontal && helpBlock }
                </div>
            );
        }
    });
};

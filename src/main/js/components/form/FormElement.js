const assign = require("object-assign");

const immutableUpdate = require("react-addons-update");

const React = require("react");

const cx = require("classnames");

const i18n = require("../../service/i18n");

const DataGraph = require("../../util/data-graph");
const DataCursor = require("../../util/data-cursor");

const FormContext = require("../../util/form-context");

const ValueLink = require("../../util/value-link");

const converter = require("../../service/property-converter");

function isValueLink(value)
{
    return value && typeof value.requestChange == "function";
}
/**
 * FormElement is a high order component for bootstrap form fields. It handles all the cursor logic and type analysis and
 * error checking.
 *
 * @param FieldComponent
 * @param [opts]        options
 * @returns {*}
 */
module.exports = function(FieldComponent, opts)
{
    //console.log("Create FormElement: ", InputComponent.displayName);

    return React.createClass({

        displayName: "FormElem(" + (FieldComponent.displayName + ")" || "Unnamed"),

        contextTypes: {
            formContext: React.PropTypes.instanceOf(FormContext)
        },

        cursorFromProps: function (props)
        {
            var value = props.value;

            //console.log("CURSOR-FROM-VALUE", value);

            if (value instanceof DataCursor)
            {
                //console.log("cursorFromProps: CURSOR ", value);
                return value;
            }
            else if (this.props.propertyType && isValueLink(value))
            {
                // return pseudo cursor
                return {
                    getPropertyType: () => this.props.propertyType,
                    value: value.value,
                    requestChange : value.requestChange
                };
            }
            else if (typeof value === "string")
            {
                //console.log("cursorFromProps:  context['" + value + "']", context);
                value = props.context.getCursor([value]);
                //console.log("NEW CURSOR", value);
                return value;
            }
            else if (value && typeof value === "object" && value.length && value[0] !== undefined)
            {
                //console.log("cursorFromProps:  context['" + value + "']", context);
                value = props.context.getCursor( value );
                //console.log("NEW CURSOR", value);
                return value;
            }
            else
            {
                if (isValueLink(value))
                {
                    throw new Error("Invalid cursor value: Link provided, but no propertyType prop set.");
                }

                throw new Error("Invalid cursor value: " + value);
            }
        },

        getInitialState: function ()
        {
            //console.log("getInitialState", FieldComponent.displayName);

            var ctx = this.context.formContext;

            var cursor = this.cursorFromProps(this.props);

            var propertyType = cursor.getPropertyType();
            var value = converter.toUser(cursor.value, propertyType);

            return {
                propertyType: propertyType,
                value : value.value,
                id: ctx.nextId(),
                errorLock: false
            };
        },

        componentWillUnmount: function ()
        {
            var ctx = this.context.formContext;
            ctx.deregister(this.state.id);
        },

        validate: function (value)
        {
            var id = this.state.id;

            //console.log("VALIDATE", value);

            var result = converter.fromUser(value, this.state.propertyType);
            var isOk = result.ok;

            var ctx = this.context.formContext;

            var localValidate = this.props.validate;
            if (isOk && localValidate)
            {
                isOk = localValidate(ctx, id, value);
            }

            var haveError = ctx.hasError(id);

            if (!isOk)
            {
                if (!haveError)
                {
                    ctx.signalError(id, result.error);
                }

                this.setState({
                    value: value,
                    errorLock: true
                });
            }
            else
            {
                if (haveError)
                {
                    ctx.signalError(id, null);
                }

                this.setState({
                    value: result.value,
                    errorLock: false
                }, function ()
                {
                    //console.log("setState end");
                    if (haveError)
                    {
                        //console.log("signal no Error");
                    }
                });
            }

            return result;
        },

        onChange: function (value)
        {
            var result = this.validate(value);

            if (result.ok)
            {
                var cursor = this.cursorFromProps(this.props);
                if (cursor.value !== result.value)
                {
                    // we use the value link compat here to be able to use pseudo cursors providing only
                    // a limited set of functionality
                    cursor.requestChange(result.value);
                    //cursor.set(null, result.value);
                }
            }
        },

        getInputField: function ()
        {
            return this._input.getInputField();
        },

        componentWillReceiveProps: function (nextProps)
        {
            //console.log("componentWillReceiveProps", JSON.stringify(nextProps));
            var id = this.state.id;
            var ctx = this.context.formContext;

            // if the field is in an error state, we cannot propagate the current erroneous value to the underlying DataGraph but instead keep it in local state only
            // in this state, we don't want props to overwrite our current state.
            if (!this.state.errorLock)
            {
                var cursor = this.cursorFromProps(nextProps);

                var propertyType = cursor.getPropertyType();

                var value = cursor.value;
                var result = converter.toUser(value, propertyType);

                //if (!result.ok)
                //{
                //    ctx.signalError(id, result.error);
                //}

                //console.log("SET NEW STATE", propertyType, result.value);

                this.setState({
                    propertyType: propertyType,
                    value: result.value
                });
            }
        },

        render: function ()
        {

            var cursor = this.cursorFromProps(this.props);

            var ctx = this.context.formContext;
            var id = this.state.id;

            var errorMessage = ctx.getErrorMessage(id);

            var pt = cursor.getPropertyType();


            var fieldComponent = (
                <FieldComponent
                    {... this.props}
                    id={ id }
                    ref={ component => this._input = component }
                    valueLink={ new ValueLink(this.state.value, this.validate) }
                    className={ this.props.className }
                    propertyType={ pt }
                    onChange={ this.onChange }
                />
            );

            if (pt.type === "Boolean")
            {
                return fieldComponent;
            }


            var labelElement = (
                <label className={ cx("control-label", ctx.labelClass(this)) } htmlFor={ id }>
                    { this.props.label || i18n(pt.parent + ":" + pt.name) }
                </label>
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

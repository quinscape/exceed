
const immutableUpdate = require("react-addons-update");

const React = require("react");

const cx = require("classnames");

const i18n = require("../../service/i18n");

const DataList = require("../../util/data-list");
const DataListCursor = require("../../util/data-list-cursor");

const FormContext = require("../../util/form-context");

const ValueLink = require("../../util/value-link");

const converter = require("../../service/property-converter");

/**
 * FormElement is a high order component for bootstrap form fields. It handles all the cursor logic and type analysis and
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

        cursorFromValue: function (value)
        {

            if (value instanceof DataListCursor)
            {
                console.log("cursorFromValue: CURSOR ", value);
                return value;
            }
            else if (typeof value === "string")
            {
                var context = this.props.context;
                console.log("cursorFromValue:  context['" + value + "']", context);
                value = context.getCursor([value]);
                console.log("NEW CURSOR", value);
                return value;
            }
            else
            {
                throw new Error("Invalid cursor value: " + value);
            }
        },

        getInitialState: function ()
        {
            var ctx = this.context.formContext;

            var cursor = this.cursorFromValue(this.props.value);
            var propertyType = cursor.getPropertyType();
            var value = converter.toUser(cursor.get(), propertyType);


            return {
                cursor: cursor,
                propertyType: propertyType,
                value : value.value,
                id: ctx.nextId(),
                errorLock: false
            };
        },

        validate: function (value)
        {
            var id = this.state.id;

            //console.log("VALIDATE", value);

            var result = converter.fromUser(value, this.state.propertyType);
            var isOk = result.ok;

            var ctx = this.context.formContext;

            var haveError = ctx.hasError(id);

            if (!isOk)
            {
                this.setState({
                    value: value,
                    errorLock: true
                }, function ()
                {
                    //console.log("setState end");
                    if (!haveError)
                    {
                        //console.log("signal Error");
                        ctx.signalError(id, result.error);
                    }
                });
            }
            else
            {
                this.setState({
                    value: result.value,
                    errorLock: false
                }, function ()
                {
                    //console.log("setState end");
                    if (haveError)
                    {
                        //console.log("signal no Error");
                        ctx.signalError(id, null);
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
                this.state.cursor.set(null, result.value);
            }
        },

        componentWillReceiveProps: function (nextProps)
        {
            console.log("FormElem", nextProps);

            var id = this.state.id;
            var ctx = this.context.formContext;

            // if the field is in an error state, we cannot propagate the current erroneous value to the underlying datalist but instead keep it in local state only
            // in this state, we don't want props to overwrite our current state.
            if (!this.state.errorLock)
            {
                var cursor = this.state.cursor;
                var propertyType = cursor.getPropertyType();

                var value = cursor.get();
                console.log("VALUE", cursor, value);
                var result = converter.toUser(value, propertyType);

                if (!result.ok)
                {
                    ctx.signalError(id, result.error);
                }

                this.setState({
                    cursor: cursor,
                    propertyType: propertyType,
                    value: result.value,
                    errorLock: !result.ok
                });
            }
        },

        render: function ()
        {

            var cursor = this.state.cursor;

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

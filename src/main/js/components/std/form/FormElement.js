/**
 * FormElement is a high order component for bootstrap form fields. It handles all the cursor logic and type analysis and
 * error checking.
 *
 * @param FieldComponent
 * @param [opts]        options
 * @returns {*}
 */
import FormContext from "../../../util/form-context";
import store from "../../../service/store";
import DataCursor from "../../../domain/cursor";
const assign = require("object-assign");

const React = require("react");

const cx = require("classnames");

const i18n = require("../../../service/i18n");

const ValueLink = require("../../../util/value-link");

const converter = require("../../../service/property-converter");

function isValueLink(value)
{
    return value && typeof value.requestChange === "function";
}
module.exports = function(FieldComponent, opts)
{
    //console.log("Create FormElement: ", InputComponent.displayName);

    return class FormElem extends React.Component {

        static displayName = "FormElem(" + ( FieldComponent.displayName || "Unnamed") + ")";

        static contextTypes =  {
            formContext: React.PropTypes.instanceOf(FormContext)
        };

        state = (() =>
        {
            //console.log("getInitialState", FieldComponent.displayName);

            const ctx = this.context.formContext;
            const cursor = this.cursorFromProps(this.props);

            const propertyType = cursor.getPropertyType();
            const value = converter.toUser(cursor.get(), propertyType);

            return {
                propertyType: propertyType,
                value : value.value,
                id: ctx.nextId(),
                errorLock: false
            };
        })();

        cursorFromProps(props)
        {
            let value = props.value;

            //console.log("CURSOR-FROM-VALUE", value);

            const propertyType = this.props.propertyType;

            if (value instanceof DataCursor)
            {
                //console.log("cursorFromProps: CURSOR ", value);
                return value;
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
                    throw new Error("Invalid cursor value: Link provided, but no propertyType prop set or provided by element config.");
                }

                throw new Error("Invalid cursor value: " + value);
            }
        }


        componentWillUnmount()
        {
            const ctx = this.context.formContext;
            ctx.deregister(this.state.id);
        }

        validate = (value) =>
        {
            const id = this.state.id;

            //console.log("VALIDATE", value);

            const result = converter.fromUser(value, this.state.propertyType);
            let isOk = result.ok;

            const ctx = this.context.formContext;

            const localValidate = this.props.validate;
            if (isOk && localValidate)
            {
                isOk = localValidate(ctx, id, value);
            }

            let haveError = ctx.hasError(id);

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
        };

        onChange = (value) =>
        {
            const result = this.validate(value);

            console.log("validate", result);

            if (result.ok)
            {
                const cursor = this.cursorFromProps(this.props);
                const cursorValue = cursor.get();
                if (cursorValue !== result.value)
                {
                    store.dispatch(
                        this.context.formContext.update(cursor, result.value)
                    );
                }
                else
                {
                    console.log("Cursor value unchanged", cursorValue);
                }
            }
        };

        getInputField()
        {
            return this._input.getInputField();
        }

        componentWillReceiveProps(nextProps)
        {
            //console.log("componentWillReceiveProps", JSON.stringify(nextProps));
            const id = this.state.id;
            const ctx = this.context.formContext;

            // if the field is in an error state, we cannot propagate the current erroneous value to the underlying DataGraph but instead keep it in local state only
            // in this state, we don't want props to overwrite our current state.
            if (!this.state.errorLock)
            {
                const cursor = this.cursorFromProps(nextProps);

                const propertyType = cursor.getPropertyType();

                const value = cursor.get();
                const result = converter.toUser(value, propertyType);

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
        }

        render()
        {

            const cursor = this.cursorFromProps(this.props);

            const ctx = this.context.formContext;
            const id = this.state.id;

            const errorMessage = ctx.getErrorMessage(id);

            const pt = cursor.getPropertyType();


            let fieldComponent = (
                <FieldComponent
                    {...this.props}
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


            const labelElement = (
                <label className={ cx("control-label", ctx.labelClass(this)) } htmlFor={ id }>
                    { this.props.label || i18n(pt.parent + ":" + pt.name) }
                </label>
            );

            let helpBlock = false;

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
    }
};

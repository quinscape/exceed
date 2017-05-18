/**
 * Action executing button
 */
import FormContext from "../../../util/form-context";
import actionService from "../../../service/action";
import i18n from "../../../service/i18n";
import cx from "classnames";
import React from "react";


class Button extends React.Component
{

    static propTypes = {
        action: React.PropTypes.func.isRequired,
        discard: React.PropTypes.bool,
        className: React.PropTypes.string,
        text: React.PropTypes.string.isRequired
    };

    static contextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    };

    constructor(props, context)
    {
        super(props, context);
        const ctx = this.context.formContext;
        this.state = { id: ctx.nextId() };
    }

    componentWillUnmount ()
    {
        var ctx = this.context.formContext;
        ctx.deregister(this.state.id);
    }


    isDisabled ()
    {
        return !this.props.discard && this.context.formContext.hasError()
    }

    render ()
    {
        var formContext = this.context.formContext;
        var id = this.state.id;
        var isDisabled = this.isDisabled();
        return (

            <input
                id={ id }
                name={ id }
                className={ cx("btn", isDisabled && "disabled", this.props.className || "btn-default") }
                type="submit"
                value={ this.props.text }
                disabled={ isDisabled }
                onClick={ (ev) => {

                    if (!this.isDisabled())
                    {
                        actionService.execute( this.props.action, false);
                    }
                    ev.preventDefault();
                } }/>
        );
    }
};

export default Button;

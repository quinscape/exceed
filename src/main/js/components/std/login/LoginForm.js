/**
 * Special form to be used in login views.
 *
 * Renders the spring security login check URI as action and provides the correct
 * CSFR-token via hidden field.
 */
import React from "react";
import uri from "../../../util/uri";
import CSFR from "../../../service/csfr";

class LoginForm extends React.Component
{
    render ()
    {
        return (
            <form action={ uri("/login_check")} method="POST">
                { this.props.children }
                <input type="hidden" name={ CSFR.tokenParam() } value={ CSFR.token() } />
            </form>
        );
    }
};

export default LoginForm

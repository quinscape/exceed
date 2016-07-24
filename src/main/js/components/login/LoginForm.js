const React = require("react");
const uri = require("../../util/uri");
const CSFR = require("../../util/csfr");

/**
 * Special form to be used in login views.
 *
 * Renders the spring security login check URI as action and provides the correct
 * CSFR-token via hidden field.
 */
var LoginForm = React.createClass({
    render: function ()
    {
        return (
            <form action={ uri("/login_check")} method="POST">
                { this.props.children }
                <input type="hidden" name={ CSFR.tokenParam() } value={ CSFR.token() } />
            </form>
        );
    }
});

module.exports = LoginForm;

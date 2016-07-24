const React = require("react");
const security = require("../../service/security");
const CSFR = require("../../service/csfr");
const i18n = require("../../service/i18n");
const uri = require("../../util/uri");

var Footer = React.createClass({
    render: function ()
    {
        const login = security.getLogin();

        return (
            <div className="container-fluid">
                <hr/>
                <div className="row">
                    <div className="col-md-8">
                        <small>{ i18n('Footer Text') }</small>
                    </div>
                    <div className="col-md-4">
                        {
                            login === "Anonymous" ?
                                <a className="btn btn-link pull-right" href={ uri("/login") }>{ i18n('Login') }</a>
                                :
                                <form className="form-inline" action={ uri('/logout') } method="POST">
                                    <input type="submit" className="btn btn-link pull-right"
                                           value={  i18n('Logout {0}', login) }/>
                                    <input type="hidden" name={ CSFR.tokenParam() } value={ CSFR.token() }/>
                                </form>
                        }
                    </div>
                </div>
            </div>
        );
    }
});

module.exports = Footer;

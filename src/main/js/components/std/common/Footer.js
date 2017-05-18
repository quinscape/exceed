import React from "react";
import security from "../../../service/security";
import CSFR from "../../../service/csfr";
import i18n from "../../../service/i18n";
import uri from "../../../util/uri";

class Footer extends React.Component {
    render ()
    {
        const login = security.getLogin();

        return (
            <div className="container-fluid page-decoration">
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
};

export default Footer

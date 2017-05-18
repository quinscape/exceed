import React from "react"
import store from "../service/store"
import i18n from "../service/i18n";

const Modal = require("react-bootstrap/lib/Modal");

import {getComponentErrors} from "../reducers/meta"

/**
 * Wraps around a child component
 */
class ComponentError extends React.Component {

    state = {
        showErrors: false
    };

    toggle = () =>
    {
        this.setState({
            showErrors: !this.state.showErrors
        })
    };

    render()
    {
        const componentId = this.props.componentId;
        const errors = getComponentErrors(store.getState(), componentId);

        const defs = [];
        let count = 0;
        errors.forEach(
            (err, index) => {
                defs.push(
                    <dt key={ count++ }>
                        <span className="right">{ "(" + err.type + ")" }</span>
                        { err.queryName }
                        </dt>,
                    <dd key={ count++ } className="text-danger">
                        { err.message } <br/>
                    </dd>
                );
            }
        );

        const numberOfErrors = errors.length;
        if (numberOfErrors)
        {
            return (
                <div className="error-container">
                    <a className="error-tag"
                       href="#error"
                       title={ i18n('Query Errors ({0})', numberOfErrors)}
                       onClick={ this.toggle  }
                    >
                        <span className="text-danger glyphicon glyphicon-exclamation-sign"/>
                    </a>
                    <Modal show={ this.state.showErrors } onHide={ this.toggle }>
                        <Modal.Header closeButton>
                            <Modal.Title>
                                { i18n("Query Errors for component '{0}'", componentId) }
                            </Modal.Title>
                        </Modal.Header>
                        <Modal.Body>
                            <dl>
                                { defs }
                            </dl>
                        </Modal.Body>
                    </Modal>
                    { React.Children.only(this.props.children) }
                </div>
            )
        }
        else
        {
            return React.Children.only(this.props.children);
        }
    }
}

export default ComponentError

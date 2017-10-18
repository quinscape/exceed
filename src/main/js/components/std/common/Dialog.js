import React from "react"

import store from "../../../service/store"
import Scope from "../../../service/scope"
import i18n from "../../../service/i18n"
import { updateScope } from "../../../actions/scope"

import Modal from "react-bootstrap/lib/Modal"

// constants for "DialogState" enum
const DIALOG_OPEN = 1;
const DIALOG_CLOSED = 0;

class Dialog extends React.Component {

    componentDidMount()
    {
        const { id, isOpen } = this.props;
        if (typeof isOpen !== "undefined")
        {
            const currentState = Scope.property(this.props.id);

            if (currentState !== isOpen)
            {
                store.dispatch(
                    updateScope( [ id ], isOpen ? DIALOG_OPEN : DIALOG_CLOSED)
                )
            }
        }
    }

    close = () => {
        const { id } = this.props;
        store.dispatch(
            updateScope( [ id ], DIALOG_CLOSED)
        )
    };

    render()
    {
        const { title, children } = this.props;
        const isOpen = Scope.property(this.props.id) !== DIALOG_CLOSED;

        return (
            <Modal show={ isOpen } onHide={ this.close }>
                <Modal.Header closeButton>
                    <Modal.Title>{ title || i18n("Dialog") }</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    { children }
                </Modal.Body>
            </Modal>
        )
    }
}

export default Dialog

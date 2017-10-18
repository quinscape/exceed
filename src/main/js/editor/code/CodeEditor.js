import React from "react";
import cx from "classnames";

const XMLEditor = require("./XMLEditor").default;
const Tokens = require("./tokens").default;

const Modal = require("react-bootstrap/lib/Modal");
import i18n from "../../service/i18n";

import sys from "../../sys";
import uri from "../../util/uri";

import Icon from "../../ui/Icon"

const update = require("immutability-helper");

import Navbar from "react-bootstrap/lib/Navbar"
import Nav from "react-bootstrap/lib/Nav"
import NavItem from "react-bootstrap/lib/NavItem"
import ScopeEditor from "./ScopeEditor"


import { selectEditorContentModel, selectEditorDocument, saveViewModels } from "../../actions/inpage"
import { getCurrentViewDocument, getViewDocuments, getCurrentViewDocumentIndex } from "../../reducers/inpage"
import EditorDocument, { CONTENT_NAMES } from "./EditorDocument"

import { storeShape } from "react-redux/lib/utils/PropTypes"

import PropTypes from 'prop-types'

function ModalControl(codeEditor)
{
    this.codeEditor = codeEditor;
}

ModalControl.prototype.open = function (renderFn, onClose)
{
    this.codeEditor.setState({
        modalOpen: true,
        renderFn: renderFn,
        onClose: onClose
    })
};


ModalControl.prototype.close = function ()
{
    this.codeEditor.close();
};

/**
 * The inpage code editor component for live-exceed application editing. Is loaded dynamically to keep the ace editor
 * out of the normal runtime bundle.
 */
class CodeEditor extends React.Component
{
    static propTypes = {
        store: storeShape,
        document: PropTypes.instanceOf(EditorDocument)
    };

    state = {
        modalOpen: false,
        renderFn: null,
        onClose: null
    };

    close = () =>
    {
        this.setState({
            modalOpen: false,
            renderFn: null
        });
        const onClose = this.state.onClose;
        onClose && onClose();
    };

    onSave = (ev) =>
    {
        const  { store } = this.props;

        const doc = this.props.document;
        ev.preventDefault();

        Tokens.syncSession(store, doc.getCurrentSession());

        // get updated document from store
        const updatedDoc = getCurrentViewDocument(store.getState());

        store.dispatch(
            saveViewModels([ updatedDoc.model ])
        );
    };

    onSaveAll = (ev) =>
    {
        const  { store } = this.props;

        const doc = this.props.document;
        ev.preventDefault();

        Tokens.syncSession(store, doc.getCurrentSession());

        // get updated document from store
        const models = getViewDocuments(store.getState()).map(doc => doc.model);

        store.dispatch(
            saveViewModels(models)
        );
    };

    changeContentTab(newContentName)
    {
        const  { store } = this.props;
        store.dispatch(
            selectEditorContentModel(newContentName)
        );
    };

    changeDocument = (ev) =>
    {
        const  { store } = this.props;

        const index = +ev.target.value;

        store.dispatch(
            selectEditorDocument(index)
        );
    };

    renderToolbar()
    {
        const state = this.props.store.getState();
        const currentDocIndex = getCurrentViewDocumentIndex(state);
        const document = getCurrentViewDocument(state);

        const allDocumentsClean = getViewDocuments(state).every(doc => doc.isClean());

        return (
            <Navbar.Form pullRight>
                <a
                    className="btn btn-link"
                    href={ uri("/editor/" + sys.appName) }
                >
                    <Icon className="glyphicon-link"/>
                    { i18n("Editor") }
                </a>
                <div className="form-group form-group-sm">
                    <label
                        htmlFor="editor-state-select"
                        className="sr-only">
                        { i18n('Current View') }
                    </label>
                    <select
                        id="editor-state-select"
                        className="form-control input-sm"
                        value={ currentDocIndex }
                        onChange={ this.changeDocument }
                    >
                        {
                            getViewDocuments(state).map((state, index) =>
                                <option key={ index } value={ index }>{ state.getLabel() }</option>
                            )
                        }
                    </select>
                </div>
                <input
                    type="submit"
                    className="btn btn-sm btn-primary"
                    value={ i18n("Save") }
                    disabled={ document.isClean() }
                    onClick={ this.onSave }
                />
                <input
                    type="submit"
                    className="btn btn-sm btn-default"
                    value={ i18n("Save All") }
                    disabled={ allDocumentsClean }
                    onClick={ this.onSave }
                />
            </Navbar.Form>
        )
    }

    render()
    {
        const { store, document, active } = this.props;

        if (!document)
        {
            return false;
        }

        const state = store.getState();

        const names = document.getContentNames();

        const doc = getCurrentViewDocument(state);
        const activeContentName = doc.currentName;
        const isScope = activeContentName === "scope";

        //console.log("CodeEditor", {doc, isScope});

        return (
            <div className="editor-body">
                <div className="navbar navbar-inverse">
                    { this.renderToolbar() }
                    <Nav bsStyle="tabs" activeKey={ activeContentName } onSelect={ contentName => this.changeContentTab(contentName) }>
                        {
                            names.map(name => <NavItem key={name} eventKey={ name }>{ i18n("Slot {0}", name) }</NavItem>)
                        }
                        <NavItem eventKey="scope"> <span className="glyphicon glyphicon-cog text-info"/> Scope</NavItem>
                        <NavItem eventKey="root"> <span className="glyphicon glyphicon-exclamation-sign text-warning"/> <span className="text-warning">Layout</span></NavItem>
                    </Nav>
                </div>

                <div className={ cx( isScope && "hidden" ) }>
                    <XMLEditor
                        store={ store }
                        document={ document }
                        modalControl={ new ModalControl(this) }
                    />
                </div>

                <ScopeEditor
                    className={ cx( !isScope && "hidden" ) }
                    store={ store }
                    document={ document }
                />

                <Modal show={ this.state.modalOpen } onHide={ this.close } enforceFocus={ false }>
                    <Modal.Header closeButton>
                        <Modal.Title>Modal heading</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        { this.state.modalOpen && this.state.renderFn() }
                    </Modal.Body>
                </Modal>
            </div>
        );
    }

}

export default CodeEditor;

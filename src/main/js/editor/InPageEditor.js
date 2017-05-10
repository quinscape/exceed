/**
 * The InPageEditor is only the outer markup shell around the actual editor that is loaded when the user activeates the
 * toggle link.
 */
import React from "react";
import classes from "classnames";
import CodeEditor from "./code/CodeEditor";
import assign from "object-assign";

const sys = require("../sys");

import { toggleEditor, EDITOR_ACTIVE_KEY } from "../actions/inpage"
import { getViewModel } from "../reducers/meta"
import { isInPageEditorActive, getViewEditorState, getCurrentViewDocument } from "../reducers/inpage"

import ComponentSubscription from "../util/ComponentSubscription"

/**
 * The outer shell of the inpage code editor. This component just handles a bit of wrapping markup and a toggle link
 * to activate/deactivate the editor.
 *
 * The actual editor contained in "./code/CodeEditor.js" is loaded dynamically to keep the ace editor classes out
 * of the main bundle.
 */
const InPageEditor = ComponentSubscription(
    class InPageEditor extends React.Component
    {
        static displayName = "InPageEditor";

        componentDidMount()
        {
            const {store} = this.props;

            const active = isInPageEditorActive(store.getState());
            const permActive = sessionStorage.getItem(EDITOR_ACTIVE_KEY) === "true";

            if (permActive || active)
            {
                this.toggle();
            }
        }

        toggle = (ev) =>
        {
            const {store} = this.props;

            store.dispatch(
                toggleEditor(
                    getViewModel(store.getState())
                )
            );

            ev && ev.preventDefault();
        };

        render()
        {
            const {store} = this.props;

            const state = store.getState();
            const active = isInPageEditorActive(state);
            const document = getCurrentViewDocument(state);

            //console.log({document,active});

            return (
                <div
                    className={
                        classes(
                            "editor",
                            active ? "active" : "inactive"
                        )
                    }
                >
                    <a
                        className="editor-tab"
                        onClick={ this.toggle }
                        href="#toggle-editor"
                        accessKey="e"
                    >
                        { "E" }
                    </a>
                    {
                        <CodeEditor
                            store={ store }
                            document={ document }
                        />
                    }
                </div>
            );
        }
    },

    (oldState, newState) => getViewEditorState(oldState) === getViewEditorState(newState)
);

module.exports = InPageEditor;

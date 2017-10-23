import React from "react"
import cx from "classnames"
import Tokens from "./tokens";
import aceLoader from "../ace-loader";
import ExceedCompleter  from "./ExceedCompleter"

import { isViewEditorClean } from "../../reducers/inpage"


class XMLEditor extends React.Component {
    intention = (editor) =>
    {
        const {store} = this.props;
        const pos = editor.getCursorPosition();

        const session = editor.getSession();
        Tokens.syncSession(store, session);
        const loc = Tokens.currentLocation(session, pos.row, pos.column, model);

//        console.log("intention", loc);
    };

    componentDidMount()
    {
        const ace = aceLoader.get();

        const langTools = ace.acequire("ace/ext/language_tools");

        const { store, document } = this.props;

        const element = this._editor;

        // extend editor from the current top position to the bottom of the window
        const r = element.getBoundingClientRect();
        element.style.height = ( window.innerHeight - r.top ) + "px";

        const editor = ace.edit(element);


        langTools.setCompleters([
            new ExceedCompleter(this, store)
        ]);

        editor.setOptions({
            fontSize: "14pt",
            showLineNumbers: false,
            enableBasicAutocompletion: true
        });
        editor.$blockScrolling = Infinity;

        editor.setSession(document.getCurrentSession());

        //editor.commands.addCommand({
        //    name: 'complete',
        //    bindKey: {win: 'Ctrl-Space',  mac: 'Command-Space'},
        //    exec: this.complete
        //});

        editor.commands.addCommand({
            name: 'intention',
            bindKey: {win: 'Alt-Return'},
            exec: this.intention
        });

        //editor.session.on("changeAnnotation", this.onChangeAnnotation);
        // editor.on("change", (o) =>{
        //
        //     const { start, end, action, lines } = o;
        //
        //     console.log("CHANGE", start, end, action, lines);
        // });

        //editor.session.getUndoManager().markClean();

        //console.log("commands", editor.commands.removeCommand);

        window.addEventListener("beforeunload", this.checkSaved, true);

        this.editor = editor;
    };

    checkSaved = (ev) =>
    {
        const { store } = this.props;
        if (!isViewEditorClean(store.getState()))
        {
            const message = 'WARNING: Lose unsaved changes in Code Editor?';
            ev.returnValue = message;
            return message;
        }
    };

    componentWillUnmount()
    {
//        console.log("unmount editor");
        this.editor.destroy();
        window.removeEventListener("beforeunload", this.checkSaved, true);

    }

    shouldComponentUpdate()
    {
        // we never update via react
        return false;
    }

    componentWillReceiveProps(nextProps)
    {
        const oldDoc = this.props.document;
        const newDoc = nextProps.document;

        const oldContent = oldDoc.currentName;
        const newContent = newDoc.currentName;

        if (oldDoc !== newDoc || oldContent !== newContent)
        {
            const editSession = newDoc.getCurrentSession();
            if (editSession)
            {
                this.editor.setSession(editSession)
            }
        }
    }

    render()
    {

        const { document , className } = this.props;
        const editorStyle = {
            height: this.props.height + "px"
        };

        const editSession = document.getCurrentSession();

        //console.log("XMLEditor", { document, className });
        
        return (
            <div
                ref={
                    c => {
                        this._editor = c;
                    }
                }
                className={ cx("code-editor", className ) }
                style={ editorStyle }
            >
                {
                    editSession.getValue()
                }
            </div>
        );
    }
}

export default XMLEditor;

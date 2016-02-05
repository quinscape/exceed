var React = require("react");
var ReactDOM = require("react-dom");

var ace = require('brace');

require('./ace-mode-exceed');

var Tokens = require("./tokens");
var ajax = require("../../service/ajax");

var toXml = require("./xml-util").toXml;

var CodeEditor = React.createClass({

    complete: function (editor)
    {
        console.log("complete", Tokens.currentLocation(editor, this.props.model));
    },

    intention: function (editor)
    {
        var pos = editor.getCursorPosition();

        var loc = Tokens.currentLocation(editor.getSession(), pos.row, pos.column , this.props.model);
        console.log("intention", loc);
    },

    componentDidMount: function ()
    {
        var editor = ace.edit(this.refs.editor);

        editor.setOption("fontSize", "14pt");
        editor.setOption("showLineNumbers", false);
        editor.$blockScrolling = Infinity;
        editor.getSession().setMode('ace/mode/exceed_view');

        editor.commands.addCommand({
            name: 'complete',
            bindKey: {win: 'Ctrl-Space',  mac: 'Command-Space'},
            exec: this.complete
        });

        editor.commands.addCommand({
            name: 'complete2',
            bindKey: {win: 'Alt-Return'},
            exec: this.intention
        });

        //editor.session.on("changeAnnotation", this.onChangeAnnotation);
        //editor.on("change", this.onChange);

        //editor.session.getUndoManager().markClean();

        //console.log("commands", editor.commands.removeCommand);

        window.addEventListener("beforeunload", this.checkSaved, true);

        this.editor = editor;
    },

    checkSaved: function (ev)
    {
        if (!this.editor.session.getUndoManager().isClean())
        {
            var message = 'WARNING: Lose unsaved changes in Code Editor?';
            ev.returnValue = message;
            return message;
        }
    },

    componentWillUnmount: function()
    {
        console.log("unmount editor");
        this.editor.destroy();
        window.removeEventListener("beforeunload", this.checkSaved, true);

    },

    componentWillReceiveProps: function (nextProps)
    {
        var newValue = toXml(nextProps.model);
        var oldValue = this.editor.getValue();
        if (oldValue !== newValue)
        {
            if (this.editor.session.getUndoManager().isClean() || window.confirm("Load external changes?"))
            {
                this.editor.setValue(newValue);
            }
        }
    },

    shouldComponentUpdate: function (nextProps, nextState)
    {
        return this.props.model !== nextProps.model;
    },
    render: function ()
    {
        var xmlDoc = toXml( this.props.model );

        console.log("render ", xmlDoc);

        return (
            <div ref="editor" className="code-editor">
                { xmlDoc }
            </div>
        );
    }
});

module.exports = CodeEditor;

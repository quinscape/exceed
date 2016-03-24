
var React = require("react");
var ReactDOM = require("react-dom");

var ace = require('brace');

require('./ace-mode-exceed');
require("brace/ext/language_tools");


var Tokens = require("./tokens");
var ajax = require("../../service/ajax");

var toXml = require("./xml-util").toXml;

var completionService = require("./completion-service");

var langTools = ace.acequire("ace/ext/language_tools");

var Modal = require("react-bootstrap/lib/Modal");

var componentService = require("../../service/component");

var CompletionType = require("./completion-type");

var bind = require("../../util/bind");
var isComponent = require("../../util/is-component");

var ID_REGEX = /<?[a-zA-Z_0-9\$\-\u00A2-\uFFFF]/;

function walk(model, indexPath)
{
    console.log("walk", model, indexPath);

    for (var i = 0; i < indexPath.length; i++)
    {

        var idx = indexPath[i];
        if (idx >= 0)
        {
            model = model.kids[idx];
        }
    }
    return model;
}

function ExceedCompleter(xmlEditor)
{
    this.xmlEditor = xmlEditor;
    bind(this, "getCompletions");
    bind(this, "insertMatch");
}

ExceedCompleter.prototype.getDocTooltip = function(selected)
{
    return selected.doc;
};

ExceedCompleter.prototype.cleanupAfter = function(editor, completion)
{
    var pos = editor.getCursorPosition();
    editor.getSelection().selectTo(pos.row, completion.start);
};

ExceedCompleter.prototype.insertMatch = function(editor, completion)
{
    //console.log("WIZARD", completion);

    var completer = this;

    if (completion.wizard)
    {
        var wizardComponent = completion.wizardComponent;

        if (isComponent(wizardComponent))
        {
            var modalControl = this.xmlEditor.props.modalControl;
            modalControl.open(function ()
            {
                return React.createElement(
                    wizardComponent, {
                        parent: completion.parentPath[0].model,
                        parentPath: completion.parentPath,
                        insert: function (snippet, options)
                        {
                            completer.cleanupAfter(editor, completion);

                            editor.insertSnippet(snippet, options);
                            modalControl.close();
                        }
                    }
                );
            }, function ()
            {
                //console.log("refocus");
                editor.focus();
                editor.gotoLine(completion.pos.row + 1, completion.pos.column);
            });
        }
        else
        {
            this.cleanupAfter(editor, completion);
            wizardComponent.call(this, editor, completion);
        }
    }
    else
    {
        this.cleanupAfter(editor, completion);
        editor.insertSnippet(completion.snippet);

        if (completion.type === CompletionType.PROP_NAME)
        {
            var pos = editor.getCursorPosition();

            editor.gotoLine(pos.row + 1, pos.column - 1);
        }
    }
};

function createParentComponentList(loc)
{
    var path = [];
    var parentPath = loc.parentPath;

    for (var i = 0; i < parentPath.length; i++)
    {
        var e = parentPath[i];
        path.unshift({
            componentName: e.model.name,
            attrs: e.model.attrs
        });
    }
    return path;
}

function createIndexPath(parentPath)
{
    var len = parentPath.length;
    var array = new Array(len);
    for (var i = 0; i <len; i++)
    {
        var e = parentPath[i];
        array[len - i - 1] = e.index;
    }
    return array;
}
ExceedCompleter.prototype.prepareCompletions = function (editor, session, componentName, propName, pos, prefix, completions)
{
    if (!completions || !completions.length)
    {
        return [];
    }

    const line = session.getLine(pos.row);

    var completionStart = prefix.length ? line.lastIndexOf(prefix, pos.column) : pos.column;
    if (completionStart < 0)
    {
        return [];
    }

    // all options should have the same type
    if (line.charAt(completionStart - 1) === "<" && completions[0].type === CompletionType.COMPONENT)
    {
        completionStart--;
    }


    for (var i = 0; i < completions.length; i++)
    {
        var completion = completions[i];

        var type = completion.type;

        if (completion.wizard)
        {
            if (type === CompletionType.COMPONENT)
            {
                componentName = completion.caption;
            }

            var componentDef = componentService.getComponents()[componentName];

            var wizardComponent;
            var wizardKey = completion.wizard.key;
            if (type === CompletionType.COMPONENT)
            {
                var templates = componentDef.templates;

                if (templates == null)
                {
                    throw new Error("Error:  Component '" + componentName + "' has no templates: " + JSON.stringify(completion))
                }

                var template = templates[wizardKey];
                if (!template)
                {
                    throw new Error("Error in component '" + componentName + "': Wizard '" + wizardKey + "' not found: " + JSON.stringify(completion))
                }

                wizardComponent = template.wizardComponent;
            }
            else if (type === CompletionType.PROP)
            {
                wizardComponent = componentDef.propWizards[wizardKey].wizardComponent;
            }
            else
            {
                throw new Error("Unknown completion type:" + type);
            }

            completion.wizardComponent = wizardComponent;

        }
        else
        {
            if (type === CompletionType.COMPONENT)
            {
                completion.snippet = toXml(completion.model);
            }
            else if (completion.type === CompletionType.PROP)
            {
                completion.snippet = completion.caption;
            }
            else if (completion.type === CompletionType.PROP_NAME)
            {
                var text = completion.caption + "=\"\"";

                // no whitespace before start point?
                if (line.charAt(completionStart - 1) > " ")
                {
                    text = " " + text;
                }
                completion.snippet = text;
            }
        }

        completion.completer = this;
        completion.start = completionStart;
    }

    return completions;
};

ExceedCompleter.prototype.getCompletions = function (editor, session, pos, prefix, callback)
{
    var loc = Tokens.currentLocation(editor.getSession(), pos.row, pos.column, this.xmlEditor.props.model);
    if (!loc)
    {
        callback(null, []);
        return;
    }

    var completionPromise;
    //console.log("loc", loc, loc.parentPath[0].model.name);

    var indexPath = createIndexPath(loc.parentPath);
    var model = Tokens.toModel(session);

    if (!model || !model.root)
    {
        return;
    }



    var componentModel = walk(model.root, indexPath);

    var componentName = componentModel.name;
    console.log("loc", loc, loc.parentPath[0].model.name, componentName);
    var propName = loc.attr;

    if (loc.attrValue)
    {
        completionPromise = completionService.autoCompleteProp( model, indexPath, propName );
    }
    else if (loc.valid)
    {
        completionPromise = completionService.autoComplete(createParentComponentList(loc), loc.parentPath[0].index);
    }
    else
    {
        completionPromise = completionService.autoCompletePropName( model, indexPath);
    }

    completionPromise.then( (completions) => {
        callback(null, this.prepareCompletions(editor, session, componentName, propName, pos, prefix, completions));
    })
    .catch(function (err)
    {
        console.error(err);
    });
};

var XMLEditor = React.createClass({

    intention: function (editor)
    {
        var pos = editor.getCursorPosition();

        var loc = Tokens.currentLocation(editor.getSession(), pos.row, pos.column , this.props.model);
        console.log("intention", loc);
    },

    componentDidMount: function ()
    {
        var editor = ace.edit(this.refs.editor);

        langTools.setCompleters([
            new ExceedCompleter(this)
        ]);

        editor.setOptions({
            fontSize: "14pt",
            showLineNumbers: false,
            enableBasicAutocompletion: true
        });
        editor.$blockScrolling = Infinity;
        editor.getSession().setMode('ace/mode/exceed_view');

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

    shouldComponentUpdate: function()
    {
        // we never update via react
        return false;
    },

    componentWillReceiveProps: function (nextProps)
    {
        var newModel = nextProps.model;
        // but we update manually if the current model is not a preview (we must have caused that preview / prevent endless loop)
        if (!newModel.preview)
        {
            // TODO: offer user option to merge
            // .. and the current undo state is clean or the user confirms that he wants to load
            if ((this.editor.session.getUndoManager().isClean() || window.confirm("Load external changes?")))
            {
                this.editor.setValue(toXml(newModel));
            }
        }
    },

    render: function ()
    {
        var xmlDoc = toXml( this.props.model );

        //console.log("render ", xmlDoc);

        return (
            <div ref="editor" className="code-editor">
                { xmlDoc }
            </div>
        );
    }
});

const CLOSED_STATE = {
    modalOpen: false,
    renderFn: null
};

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

var CodeEditor = React.createClass({

    getInitialState: function ()
    {
        return CLOSED_STATE;
    },
    close: function ()
    {
        this.setState(CLOSED_STATE);
        var onClose = this.state.onClose;
        onClose && onClose();
    },
    render: function ()
    {
        return (
            <div>
                <XMLEditor modalControl={ new ModalControl(this) } model={ this.props.model} />
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
});

module.exports = CodeEditor;

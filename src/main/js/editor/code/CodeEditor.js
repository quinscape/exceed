
var React = require("react");
var ReactDOM = require("react-dom");

var ace = require('brace');

require('./ace-mode-exceed');
require("brace/ext/language_tools");

var ExceedViewMode = ace.acequire("ace/mode/exceed_view").Mode;
var EditSession = ace.acequire("ace/edit_session").EditSession;

var Tokens = require("./tokens");
var ajax = require("../../service/ajax");

var toXml = require("./xml-util").toXml;

var completionService = require("./completion-service");

var langTools = ace.acequire("ace/ext/language_tools");

var Modal = require("react-bootstrap/lib/Modal");

var componentService = require("../../service/component");
var i18n = require("../../service/i18n");

var CompletionType = require("./completion-type");

var bind = require("../../util/bind");
var isComponent = require("../../util/is-component");

var LinkedStateMixin = require("react-addons-linked-state-mixin");

var update = require("react-addons-update");

var ID_REGEX = /<?[a-zA-Z_0-9\$\-\u00A2-\uFFFF]/;

var hub = require("../../service/hub");

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
    editor.getSelection().selectTo(completion.start.row, completion.start.column);
};

ExceedCompleter.prototype.insertMatch = function(editor, completion)
{
    console.log("INSERT MATCH", completion);

    var completer = this;

    var wizardComponent = completion.wizardComponent;
    if (wizardComponent)
    {
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

        // if we're a prop completion that had a template, replace the component
        if (completion.type === CompletionType.PROP && completion.model && !editor.getSession().getAnnotations().length)
        {
            var selection = editor.getSelection();
            var range = selection.getRange();
            range.setEnd(completion.end.row, completion.end.column);
            selection.setSelectionRange(range);
        }

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

ExceedCompleter.prototype.prepareCompletions = function (editor, session, componentName, propName, pos, prefix, componentModel, completions)
{
    if (!completions || !completions.length)
    {
        return [];
    }

    var line = session.getLine(pos.row);

    var start = {
        row: pos.row,
        column: prefix.length ? line.lastIndexOf(prefix, pos.column) : pos.column
    };

    if (start.column < 0)
    {
        return [];
    }

    // all options should have the same type
    if (line.charAt(start.column - 1) === "<" && completions[0].type === CompletionType.COMPONENT)
    {
        start.column--;
    }


    for (var i = 0; i < completions.length; i++)
    {
        var completion = completions[i];

        console.log("COMPLETION", completion);

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

                if (completion.model && !completion.snippet)
                {
                    completion.snippet = toXml(completion.model);
                }
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
            if (completion.type === CompletionType.PROP)
            {
                if (completion.model)
                {
                    start = componentModel.pos.start;
                    completion.end = componentModel.pos.end;

                    completion.snippet = toXml(completion.model);
                }
                else
                {
                    completion.snippet = completion.caption;
                }
            }
            else if (completion.type === CompletionType.PROP_NAME)
            {
                var text = completion.caption + "=\"\"";

                // no whitespace before start point?
                if (line.charAt(start.column - 1) > " ")
                {
                    text = " " + text;
                }
                completion.snippet = text;
            }
        }

        completion.completer = this;
        completion.start = start;
    }

    return completions;
};

ExceedCompleter.prototype.getCompletions = function (editor, session, pos, prefix, callback)
{
    var model = Tokens.toModel(session, true);
    var loc = Tokens.currentLocation(editor.getSession(), pos.row, pos.column, model);
    if (!loc)
    {
        callback(null, []);
        return;
    }

    var completionPromise;
    //console.log("loc", loc, loc.parentPath[0].model.name);

    var indexPath = createIndexPath(loc.parentPath);

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
        completionPromise = completionService.autoComplete(model, createParentComponentList(loc), loc.parentPath[0].index);
    }
    else
    {
        completionPromise = completionService.autoCompletePropName( model, indexPath);
    }

    completionPromise.then( (completions) => {
        callback(null, this.prepareCompletions(editor, session, componentName, propName, pos, prefix, componentModel, completions));
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

        var session = editor.getSession();
        var model = Tokens.toModel(session);
        var loc = Tokens.currentLocation(session, pos.row, pos.column , model);
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
        editor.setSession(this.props.editorState.editSession);

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
        var undoManager = this.editor.session.getUndoManager();
        if (undoManager && !undoManager.isClean())
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
        var oldState = this.props.editorState;
        var newState = nextProps.editorState;
        if (oldState !== newState)
        {
            this.editor.setSession(newState.editSession)
        }
    },

    render: function ()
    {
        //console.log("render ", xmlDoc);

        return (
            <div ref="editor" className="code-editor">
                { this.props.editorState.editSession.getValue() }
            </div>
        );
    }
});

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

function EditorState(model)
{
    this.viewName = model.name;
    this.editSession = new EditSession(toXml(model), new ExceedViewMode());
    this.editSession.setUndoManager(new ace.UndoManager());
    this.editSession.exceedViewName = this.viewName;
    this.changed = null;
}


EditorState.prototype.isClean = function ()
{
    return this.editSession.getUndoManager().isClean();
};

EditorState.prototype.getLabel = function ()
{
    return "View '" + this.viewName + "'" +
        (this.changed ? i18n("Changed") + " " + this.changed.toISOString() : "")
};

var Toolbar = React.createClass({
    render: function ()
    {
        return (
            <form className="form-inline" action="#" onSubmit={ this.props.onSave }>
                <div className="form-group-sm">
                    <label className="sr-only" htmlFor="editor-state-select">View</label>
                    <select id="editor-state-select" className="form-control input-sm" valueLink={ this.props.stateLink }>
                        {
                            this.props.editorStates.map( (state, index) =>
                                <option key={ index} value={ index }>{ state.getLabel() }</option>
                            )
                        }
                    </select>
                    <input type="submit" className="btn btn-sm btn-primary" value="Save" />
                </div>
            </form>
        );
    }
});

const VIEW_RECYCLE_LIMIT = 8;

var CodeEditor = React.createClass({

    mixins: [ LinkedStateMixin ],

    getInitialState: function ()
    {
        return {
            editorStates:  [ new EditorState(this.props.model) ],
            currentState: 0,

            modalOpen: false,
            renderFn: null
        };
    },
    close: function ()
    {
        this.setState({
            modalOpen: false,
            renderFn: null
        });
        var onClose = this.state.onClose;
        onClose && onClose();
    },

    onSave: function (ev)
    {
        var currentState = this.getCurrentState();
        var model = Tokens.toModel(currentState.editSession);

        ev.preventDefault();
        if (!model || !model.root)
        {
            return;
        }

        var viewName = currentState.viewName;
        var json = JSON.stringify(model, null, "    ");
        console.log(json);

        return hub.request({
            type: "message.SaveViewRequest",
            viewName: viewName,
            document: json
        }).then(function (data)
        {
            console.log("SAVED", data);
            currentState.editSession.getUndoManager().markClean();
        })
        .catch(function (err)
        {
            console.error(err);
        });
    },

    componentWillReceiveProps: function (nextProps)
    {
        var oldModel = this.props.model;
        var newModel = nextProps.model;
        // but we update manually if the current model is not a preview (we must have caused that preview / prevent endless loop)
        if (!newModel.preview && newModel.version !== oldModel.version)
        {
            var editorStates = this.state.editorStates;

            var i, state, nextStateIndex;

            // We start with adding the new state to the end of the list
            nextStateIndex = editorStates.length;

            // then we see if we're already editing the same view
            for (i = 0; i < nextStateIndex; i++)
            {
                state = editorStates[i];
                if (state.viewName === newModel.viewName && !state.changed)
                {
                    // and there are no changes in that view or the user confirms the overwrite
                    if (state.isClean())
                    {
                        // if so, we reuse that editor index
                        nextStateIndex = i;
                        break;
                    }
                    else
                    {
                        state.changed = Date.now();
                    }
                }
            }

            // if we haven't found the view and we're above our limit for view recycling
            if (nextStateIndex === editorStates.length && nextStateIndex >= VIEW_RECYCLE_LIMIT)
            {
                for (i = 0; i < nextStateIndex; i++)
                {
                    state = editorStates[i];

                    // check if we have an editor that has no unsaved changes
                    if (state.isClean())
                    {
                        // .. and reuse that
                        nextStateIndex = i;
                        break;
                    }
                }
            }

            var newState = new EditorState(newModel);
            this.setState({
                editorStates: update(this.state.editorStates, {
                    [nextStateIndex] : { $set : newState }
                }),
                currentState: nextStateIndex
            });
            //
            //// TODO: offer user option to merge
            //// .. and the current undo state is clean or the user confirms that he wants to load
            //var undoManager = this.editor.session.getUndoManager();
            //if (undoManager.isClean() || window.confirm("Load external changes?"))
            //{
            //    this.editor.setValue(toXml(newModel));
            //    this.editor.getSession().setUndoManager(new ace.UndoManager())
            //}
        }
    },

    getCurrentState: function ()
    {
        return this.state.editorStates[this.state.currentState];
    },

    render: function ()
    {
        return (
            <div>
                <Toolbar
                    stateLink={ this.linkState("currentState") }
                    editorStates={ this.state.editorStates }
                    onSave={ this.onSave }
                />
                <XMLEditor modalControl={ new ModalControl(this) } editorState={ this.getCurrentState() } />
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

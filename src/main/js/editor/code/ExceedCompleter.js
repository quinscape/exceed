import React from "react"
import Tokens from "./tokens"

import completionService from "./completion-service";

import CompletionType from "./completion-type";
const isComponent = require("../../util/is-component");
import componentService from "../../service/component"

import { getCurrentViewDocument } from "../../reducers/inpage"

function createParentComponentList(loc)
{
    const path = [];
    const parentPath = loc.parentPath;

    for (let i = 0; i < parentPath.length; i++)
    {
        const e = parentPath[i];
        path.unshift({
            componentName: e.model.name,
            attrs: e.model.attrs
        });
    }
    return path;
}

function createIndexPath(parentPath)
{
    const len = parentPath.length;
    const array = new Array(len);
    for (let i = 0; i <len; i++)
    {
        const e = parentPath[i];
        array[len - i - 1] = e.index;
    }
    return array;
}

function walkComponent(component, path)
{
    //console.log("WALK", obj, path);

    const root = component;

    let i;
    const len = path.length;
    for (i = 0; i < len; i++)
    {
        const idx = path[i];
        if (idx >= 0)
        {
            component = component.kids[idx];
        }

        if (!component)
        {
            throw new Error("Error walking " + path + " from " + JSON.stringify(root));
        }
    }
    return component;
}

class ExceedCompleter
{
    constructor(xmlEditor, store)
    {
        this.xmlEditor = xmlEditor;
        this.store  = store;
    }

    getDocTooltip(selected)
    {
        return selected.doc;
    }

    cleanupAfter(editor, completion)
    {
        editor.getSelection().selectTo(completion.start.row, completion.start.column);
    }

    insertMatch = (editor, completion) =>
    {
        console.log("INSERT MATCH", completion);

        const completer = this;

        const wizardComponent = completion.wizardComponent;
        if (wizardComponent)
        {
            if (isComponent(wizardComponent))
            {
                const modalControl = this.xmlEditor.props.modalControl;
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
                const selection = editor.getSelection();
                const range = selection.getRange();
                range.setEnd(completion.end.row, completion.end.column);
                selection.setSelectionRange(range);
            }

            editor.insertSnippet(completion.snippet);

            if (completion.type === CompletionType.PROP_NAME)
            {
                const pos = editor.getCursorPosition();

                editor.gotoLine(pos.row + 1, pos.column - 1);
            }
        }
    }

    prepareCompletions(editor, session, componentName, propName, pos, prefix, componentModel, completions)
    {
        if (!completions || !completions.length)
        {
            return [];
        }

        const line = session.getLine(pos.row);

        let start = {
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


        for (let i = 0; i < completions.length; i++)
        {
            const completion = completions[i];

            console.log("PREPARE COMPLETION", completion);

            const type = completion.type;

            if (completion.wizard)
            {
                if (type === CompletionType.COMPONENT)
                {
                    componentName = completion.caption;
                }

                const componentDef = componentService.getComponents()[componentName];

                let wizardComponent;
                const wizardKey = completion.wizard.key;
                if (type === CompletionType.COMPONENT)
                {
                    const templates = componentDef.templates;

                    if (!templates)
                    {
                        throw new Error("Error:  Component '" + componentName + "' has no templates: " + JSON.stringify(completion))
                    }

                    let template = templates[wizardKey];
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
                if (completion.type === CompletionType.COMPONENT && completion.model && !completion.snippet)
                {
                    completion.snippet = toXml(completion.model);
                }
                else if (completion.type === CompletionType.PROP)
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
                    let text = completion.caption + "=\"\"";

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
    }

    getCompletions = (editor, session, pos, prefix, callback) =>
    {
        const { store } = this;

        Tokens.syncSession(store, session, true);

        const document = getCurrentViewDocument(store.getState());
        const model = document.model;
        const contentName = document.getCurrentSession().exceedContentName;

        let loc = Tokens.currentLocation(editor.getSession(), pos.row, pos.column, model);
        if (!loc)
        {
            callback(null, []);
            return;
        }

        let completionPromise;
        //console.log("loc", loc, loc.parentPath[0].model.name);

        const indexPath = createIndexPath(loc.parentPath);
        const componentModel = walkComponent(model.content[contentName], indexPath);

        const componentName = componentModel.name;
        console.log("loc", loc, loc.parentPath[0].model.name, componentName);

        const propName = loc.attr;

        if (loc.attrValue)
        {
            completionPromise = completionService.autoCompleteProp( model, { path: indexPath, content: contentName}, propName );
        }
        else if (loc.valid)
        {
            completionPromise = completionService.autoComplete(model, createParentComponentList(loc), loc.parentPath[0].index);
        }
        else
        {
            completionPromise = completionService.autoCompletePropName( model, { path: indexPath, content: contentName});
        }

        completionPromise.then( (completions) => {
            callback(null, this.prepareCompletions(editor, session, componentName, propName, pos, prefix, componentModel, completions));
        })
            .catch(function (err)
            {
                console.error(err);
            });
    }
}

export default ExceedCompleter

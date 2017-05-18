import Memoizer from "../../util/memoizer"
import toExternal from "../../util/to-external"
import { toXml } from "./xml-util";
import uuid from "uuid"
import keys from "../../util/keys"

import update from "react-addons-update"

import i18n from "../../service/i18n"


export const CONTENT_NAMES = Memoizer((root) => keys(EditorViewDocument.findContentRefs(root, [])).sort());

let layoutEditSessions = {};

const DEFAULT = "__DEFAULT__";

function createLayoutEditSession(doc)
{
    const name = doc.model.layout || DEFAULT;
    const editSession = layoutEditSessions[name];
    if (!editSession)
    {
        return layoutEditSessions[name] = doc.createSession("root");
    }
    return editSession;
}

/**
 * Document for the inpage editor. Encapsulates the current model and an ace editor session per content model in the
 * model, determined by the <Content/> references in the "root" layout content of the view.
 *
 * We update the parts we have control over immutably. Since the embedded edit sessions are not updated immutably by ACE
 * , we cannot really keep this class completely immutable.  
 */
class EditorViewDocument {
    /**
     * Constructs a new editor document from the given source
     *
     * @param source    {ViewModel|EditorViewDocument} either a view model to create a document for or an editor document to copy.
     */
    constructor(ace, source)
    {
        if (!ace)
        {
            throw new Error("Need ace API");
        }
        if (!source)
        {
            throw new Error("Need view model or EditorDocument as source");
        }

        this.ace = ace;
        this.changed = Date.now();

        if (source instanceof EditorViewDocument)
        {
            // copy constructor
            this.model = source.model;
            this.viewName = source.viewName;
            this.editSessions = source.editSessions;
            this.currentName = source.currentName;
            this.contentNames = source.contentNames;
        }
        else
        {
            console.debug("Editor Document: Create from", source);

            const model = toExternal(source);
            this.model = model;
            this.viewName = model.name;
            this.currentName = "main";

            const contentNames = CONTENT_NAMES(model.content.root);
            this.contentNames = contentNames;

            let editSessions = {
                root: createLayoutEditSession(this)
            };
            contentNames.forEach(name =>
            {
                editSessions[name] = this.createSession(name);
            });
            this.editSessions = editSessions;
        }
    }

    createSession(contentName)
    {
        const ExceedViewMode = this.ace.acequire("ace/mode/exceed_view").Mode;
        const EditSession = this.ace.acequire("ace/edit_session").EditSession;

        const editSession = new EditSession(toXml(this.model, contentName), new ExceedViewMode());

        editSession.setUndoManager(new this.ace.UndoManager());
        editSession.exceedViewName = this.viewName;
        editSession.exceedContentName = contentName;
        editSession.suppressPreviewOnce = true;

        return editSession;
    }

    withCurrentContent(newName)
    {
        const copy = new EditorViewDocument(this.ace, this);
        copy.currentName = newName;
        return copy;
    }

    markClean()
    {
        const { editSessions } = this;

        const copy = new EditorViewDocument(this.ace, this);

        const newSessions = {};

        for (let name in editSessions)
        {
            if (editSessions.hasOwnProperty(name))
            {
                let session = editSessions[name];
                if(session)
                {
                    session.getUndoManager().markClean();
                }

                newSessions[name] = session;
            }
        }

        copy.editSessions = newSessions;

        return copy;
    }

    isClean()
    {
        const { editSessions } = this;
        for (let name in editSessions)
        {
            if (editSessions.hasOwnProperty(name))
            {
                let session = editSessions[name];
                if(session && !session.getUndoManager().isClean())
                {
                    return false;
                }
            }
        }
        return true;
    }

    withContentModel(contentName, contentModel)
    {
        const copy = new EditorViewDocument(this.ace, this);

        if (contentName === "root")
        {
            const newContentNames = CONTENT_NAMES(contentModel);

            console.log({newContentNames});
            copy.contentNames = newContentNames;

            const newEditSessions = {
                root: copy.getCurrentSession()
            };

            newContentNames.forEach(name =>
            {
                const editSession = this.editSessions[name];

                if (editSession)
                {
                    console.log("copy session for ", name);
                    newEditSessions[name] = editSession;
                }
                else
                {
                    console.log("new session for ", name);
                    if (!copy.model.content[name])
                    {
                        copy.model = update(copy.model, {
                            content: {
                                [name]: {
                                    $set: {
                                        name: "b",
                                        kids: [
                                            {
                                                "name": "[String]",
                                                "attrs": {
                                                    "value": "New Content'" + name + "'"
                                                }
                                            }
                                        ]
                                    }
                                }
                            }
                        });
                    }

                    newEditSessions[name] = copy.createSession(name);
                }
            });
            copy.editSessions = newEditSessions;

            console.log({copy});
        }

        copy.model = update(copy.model, {
            versionGUID: {$set: uuid.v4()},
            content: {
                [contentName] : { $set: contentModel }
            }
        });
        return copy;
    }

    getContentNames()
    {
        return this.contentNames;
    }

    getCurrentSession()
    {
        return this.editSessions[this.currentName];
    }

    getLabel()
    {
        return "View '" + this.viewName + "'" +( !this.isClean() ? "*" : "")
    }

    static findContentRefs(component, contentNames)
    {
        const { name, kids } = component;

        if (name === "Content")
        {
            // name attribute if exist or "main" as default
            const contentRef = (component.attrs && component.attrs.name) || "main";
            contentNames[contentRef] = true;
            return;
        }

        if (kids)
        {
            for (let i = 0; i < kids.length; i++)
            {
                EditorViewDocument.findContentRefs(kids[i], contentNames);
            }
        }

        return contentNames;
    }

    static getLayoutEditSession(layoutName)
    {

        return layoutEditSessions[layoutName || DEFAULT];
    }

    static clearLayoutEditSessions()
    {
        layoutEditSessions = {};
    }

}



export default EditorViewDocument

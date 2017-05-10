import React from "react";
import ComponentSubscription from "../util/ComponentSubscription";
import UndoManager from "../editor/UndoManager";

import ModelSelector from "./ModelSelector";
import ConfigEditor from "./ConfigEditor";
import ModelDocs from "./ModelDocs";
import AutoHeight from "../ui/AutoHeight";
import ButtonToolbar from "react-bootstrap/lib/ButtonToolbar";
import ButtonGroup from "react-bootstrap/lib/ButtonGroup";
import Button from "react-bootstrap/lib/Button";

import { getCurrentEditorLocation, getFilter } from "../reducers/editor/editorView";
import {
    getAppConfig,
    getDomainType,
    getEnumType,
    getLayout,
    getProcess,
    getRoutingTable,
    getView
} from "../reducers/editor";

const i18n = require("../service/i18n");

const MODEL_SELECTORS = {
    config: getAppConfig,
    routing: getRoutingTable,
    domainType: getDomainType,
    enumType: getEnumType,
    process: getProcess,
    view: getView,
    layout: getLayout
};

function JSONDump(props)
{
    const { model } = props;
    
    return (
        <pre>
            { JSON.stringify(model,null,4)}
        </pre>
    );
}

function getModelSelector(type)
{
    const selector = MODEL_SELECTORS[type];

    if (!selector)
    {
        throw new Error("No selector for type '" + type + "'");
    }

    return selector;
}

/**
 *
 * @name EditorLocation
 * @type {{
 *     type: string,
 *     name: string?,
 *     resultType: string?
 *     detail: string|object
 * }}
 */

/**
 * Editor main component
 *
 * @type {ReactComponent}
 */
const Editor = ComponentSubscription(
    AutoHeight(class extends React.Component {

        open(type,path)
        {
            console.log({type,path});
        }

        save = ev =>
        {
            console.log("SAVE");
        };


        render()
        {
            const { height, store } = this.props;

            const state = store.getState();
            const currentLocation = getCurrentEditorLocation(state);
            const currentModel = getModelSelector(currentLocation.type)(state, currentLocation.name);

            const editorPaneComponent = MODEL_EDITORS[currentLocation.type] || JSONDump;

            return (
                <div className="main-editor container-fluid">
                    <div className="row">
                        <ModelSelector store={ this.props.store } currentLocation={ currentLocation } filter={ getFilter(state) } height={ height }/>

                        <div className="model-container col-md-10" style={{ height: height }}>
                            <div className="btn-toolbar" style={{padding: "5px 0"}}>
                                <Button
                                    disabled={ !UndoManager.canUndo()}
                                    onClick={ UndoManager.undo}>
                                    { i18n("Undo")}
                                </Button>
                                <Button
                                    disabled={ !UndoManager.canRedo()}
                                    onClick={ UndoManager.redo}>
                                    { i18n("Redo")}
                                </Button>
                                <Button
                                    disabled={ UndoManager.isClean()}
                                    bsStyle="primary"
                                    onClick={ this.save }>
                                >
                                    { i18n("Save")}
                                </Button>
                            </div>
                            {
                                React.createElement(editorPaneComponent, {
                                    store: store,
                                    location: currentLocation,
                                    model: currentModel
                                })
                            }
                        </div>
                    </div>

                </div>
            )
        }

    })
);

const MODEL_EDITORS = {
    "config" : ConfigEditor,
    "model-docs" : ModelDocs

};

export default Editor

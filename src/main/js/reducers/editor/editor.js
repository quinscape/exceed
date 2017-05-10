import { EDITOR_CONFIG_UPDATE } from "../../actions/editor/config"

export default function(editorState = {}, action)
{
    if (action.type === EDITOR_CONFIG_UPDATE)
    {
        return action.graph;
    }

    return editorState;
}

export function getEditorData(state)
{
    return state.editor;
}

export function getAppConfig(state)
{
    return state.editor.rootObject.config;
}


export function getViews(state)
{
    return state.editor.rootObject.view;
}


export function getView(state, name)
{
    const view = getViews(state)[name];
    //console.log("VIEW", name, "=>", view, getViews(state));

    return view;
}


export function getRoutingTable(state)
{
    return state.editor.rootObject.routing;
}



export function getProcesses(state)
{
    return state.editor.rootObject.process;
}


export function getProcess(state, name)
{
    return getProcesses(state)[name];
}

export function getLayouts(state)
{
    return state.editor.rootObject.layout;
}


export function getLayout(state, name)
{
    return getLayouts(state)[name];
}


export function getEnumTypes(state)
{
    return state.editor.rootObject.enumType;
}


export function getEnumType(state, name)
{
    return getEnumTypes(state)[name];
}


export function getDomainTypes(state)
{
    return state.editor.rootObject.domainType;
}


export function getDomainType(state, name)
{
    return getDomainTypes(state)[name];
}



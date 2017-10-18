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
    return getEditorData(state).rootObject.config;
}


export function getViews(state)
{
    return getEditorData(state).rootObject.view;
}


export function getView(state, name)
{
    const view = getViews(state)[name];
    //console.log("VIEW", name, "=>", view, getViews(state));

    return view;
}


export function getRoutingTable(state)
{
    return getEditorData(state).rootObject.routing;
}



export function getProcesses(state)
{
    return getEditorData(state).rootObject.process;
}


export function getProcess(state, name)
{
    return getProcesses(state)[name];
}

export function getLayouts(state)
{
    return getEditorData(state).rootObject.layout;
}


export function getLayout(state, name)
{
    return getLayouts(state)[name];
}


export function getEnumTypes(state)
{
    return getEditorData(state).rootObject.enumType;
}


export function getEnumType(state, name)
{
    return getEnumTypes(state)[name];
}


export function getDomainTypes(state)
{
    return getEditorData(state).rootObject.domainType;
}


export function getDomainType(state, name)
{
    return getDomainTypes(state)[name];
}


export function getTranslations(state)
{
    return getEditorData(state).rootObject.translation;
}

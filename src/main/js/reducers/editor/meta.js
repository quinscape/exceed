import { getMeta } from "../meta"

export function getResourceLocations(state)
{
    return getMeta(state).editor.resources;
}

export function getModelDocs(state)
{
    return getMeta(state).modelDocs;
}

export function getModelLocations(state)
{
    return getMeta(state).modelLocations;
}

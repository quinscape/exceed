/**
 * Identity returning reducer for the read-only parts of our redux state.
 *
 * @param meta              state meta data
 * @param action            action
 * @returns {object} the same state
 */
export default function(meta = {}, action)
{
    // meta state is only changing via full state replacement ( see root reducer)
    return meta;
}

export function getMeta(state)
{
    return state.meta;
}

export function getAppName(state)
{
    return getMeta(state).appName;
}

export function getDomain(state)
{
    return getMeta(state).domain;
}

export function getDomainTypes(state)
{
    return getMeta(state).domain.domainTypes;
}

export function getDomainType(state,name)
{
    return getDomainTypes(state)[name];
}

export function getEnumType(state,name)
{
    return getMeta(state).domain.enumTypes[name];
}


export function getAuthentication(state)
{
    return getMeta(state).authentication;
}

export function getConnectionId(state)
{
    return getMeta(state).connectionId;
}

export function getContextPath(state)
{
    return getMeta(state).contextPath;
}

export function getLocation(state)
{
    return getMeta(state).location;
}


export function getTranslationTemplate(state, name)
{
    return getMeta(state).translations[name];
}

export function getViewState(state)
{
    return getMeta(state).viewState;
}

export function getActionNames(state)
{
    return getMeta(state).actionNames;
}

export function getTokenInfo(state)
{
    return getMeta(state).token;
}

export function getPropertyModel(state, type, name)
{
    const domainType = getDomainType(state, type);
    const properties = domainType && domainType.properties;
    if (properties)
    {
        for (let i = 0; i < properties.length; i++)
        {
            const prop = properties[i];
            if (prop.name === name)
            {
                return prop;
            }
        }
    }
    return null;
}

export function getComponentErrors(state, id)
{
    return getMeta(state).componentError.errors[id] || [];
}


export function getViewModel(state)
{
    return getMeta(state).model;
}

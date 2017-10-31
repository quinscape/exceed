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
    return getMeta(state).config.appName;
}

/**
 * Returns a currency
 *
 * @param state             redux state
 * @param propertyType      {?PropertyModel} propertyModel if given, evaluate for that property, otherwise return app default
 * @returns {*}
 */
export function getCurrency(state, propertyType)
{
    if (propertyType && propertyType.type !== "Currency")
    {
        throw new Error("Invalid property: " + JSON.stringify(propertyType));
    }

    return (propertyType && propertyType.typeParam ) || getMeta(state).config.defaultCurrency;
}

export function getDomainData(state)
{
    return getMeta(state).domain;
}

export function getDomainTypes(state)
{
    return getDomainData(state).domainTypes;
}

export function getDomainType(state,name)
{
    return getDomainTypes(state)[name];
}

export function getStateMachine(state,name)
{
    return getDomainData(state).stateMachines[name];
}

export function getEnumType(state,name)
{
    return getDomainData(state).enumTypes[name];
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

export function getViewStateTransition(state, name)
{
    const viewState = getViewState(state);

    if (!viewState)
    {
        throw new Error("No view-state with '" + name + "'");
    }

    return viewState.transitions[name];
}

export function getActionNames(state)
{
    return getMeta(state).actionNames;
}

export function getTokenInfo(state)
{
    return getMeta(state).token;
}

export function getComponentErrors(state, id)
{
    return getMeta(state).componentError.errors[id] || [];
}


export function getViewModel(state)
{
    return getMeta(state).model;
}

export function getRoutingTable(state)
{
    return getMeta(state).routing;
}

export function getComponentConfig(state, name)
{
    const config = getMeta(state).config.component;
    if (!config.hasOwnProperty(name))
    {
        throw new Error("Invalid configuration '" + name + '. Every configuration name must be a property in de.quinscape.exceed.model.config.ComponentConfig."');
    }
    return config[name];
}

package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;

/**
 * Handles the write-back of query type types.
 */
public interface QueryTypeUpdateHandler
{
    /**
     * Updates the given domain object
     *
     * @param runtimeContext    runtime context
     * @param domainObject      domain object
     * @param updateType        type of update
     */
    void update(
        RuntimeContext runtimeContext,
        DomainObject domainObject,
        UpdateType updateType
    );
}

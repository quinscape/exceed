package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.Set;

/**
 * Represents a domain object within the modeled application domain.
 */
public interface DomainObject
{
    String getId();

    void setId(String id);

    DomainService getDomainService();

    void setDomainService(DomainService domainService);

    String getDomainType();

    void setDomainType(String type);

    Set<String> propertyNames();

    Object getProperty(String name);

    void setProperty(String name, Object value);

    /**
     * Inserts the object into the storage or updates it if it already exists. Note that this usually entails re-reading
     * the object from storage. So if you know whether the object exists or not, you might be better off calling the
     * appropriate method directly.
     *
     * @param runtimeContext    runtime context
     */
    default void insertOrUpdate(RuntimeContext runtimeContext)
    {
        getDomainService().insertOrUpdate(runtimeContext, this);
    }

    /**
     * Updates the given domain object by its primary key.
     *
     * @param runtimeContext    runtime context
     *
     * @return <code>true</code> if exactly one object was updated.
     */
    default boolean update(RuntimeContext runtimeContext)
    {
        return getDomainService().update(runtimeContext, this);
    }

    /**
     * Inserts the domain object into storage.
     *
     * @param runtimeContext    runtime context
     */
    default void insert(RuntimeContext runtimeContext)
    {
        getDomainService().insert(runtimeContext, this);
    }

    /**
     * Deletes the given domain object by its primary key.
     *
     * @param runtimeContext    runtime context
     *
     * @return <code>true</code> if exactly one object was deleted.
     */
    default boolean delete(RuntimeContext runtimeContext)
    {
        return getDomainService().delete(runtimeContext, this);
    }
}

package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;

/**
 *  Handles registration of domain types in the system and converts domain objects to JSON and back.
 */
public interface DomainService
    extends DomainTypesRegistry
{
    void init(RuntimeApplication runtimeApplication, String schema);

    String toJSON(Object domainObject);

    <T> T toDomainObject(Class<T> cls, String json);

    DomainType getDomainType(String name);

    String getSchema();

    DomainObject create(RuntimeContext runtimeContext, String type, String id);

    DomainObject create(RuntimeContext runtimeContext, String type, String id, Class<? extends DomainObject> implClass);

    DomainObject read(RuntimeContext runtimeContext, String type, String id);

    boolean delete(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    void insert(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    void insertOrUpdate(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    boolean update(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    StorageConfiguration getStorageConfiguration(String domainType);

    JsEnvironment getJsEnvironment();
}

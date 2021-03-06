package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.js.JsEnvironment;

import java.util.Map;

/**
 *  Handles registration of domain types in the system and converts domain objects to JSON and back.
 */
public interface DomainService
    extends DomainTypesRegistry
{
    void init(
        RuntimeApplication runtimeApplication,
        Map<String, ExceedDataSource> dataSources
    );

    String toJSON(Object domainObject);

    <T> T toDomainObject(Class<T> cls, String json);

    DomainType getDomainType(String name);

    String getSchema();

    String getAuthSchema();

    DomainObject create(RuntimeContext runtimeContext, String type, String id);

    DomainObject create(RuntimeContext runtimeContext, String type, String id, Class<? extends DomainObject> implClass);

    DomainObject read(RuntimeContext runtimeContext, String type, String id);

    boolean delete(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    void insert(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    void insertOrUpdate(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    boolean update(RuntimeContext runtimeContext, DomainObject genericDomainObject);

    JsEnvironment getJsEnvironment();

    /**
     * Returns the data source with the given name. If the name is <code>null</code>, the default data source is returned.
     *
     * @param dataSourceName    name or <code>null</code> for default
     * @return  data source
     */
    ExceedDataSource getDataSource(String dataSourceName);

    Map<String,ExceedDataSource> getDataSources();
}

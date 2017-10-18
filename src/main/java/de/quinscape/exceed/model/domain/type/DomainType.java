package de.quinscape.exceed.model.domain.type;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.runtime.domain.DomainService;

import java.util.List;

public interface DomainType
    extends TopLevelModel, AutoVersionedModel
{
    String ID_PROPERTY = "id";
    /**
     * Storage used for internal data types.
     */
    String SYSTEM_STORAGE = "systemStorage";
    /**
     * Default storage for non-system types
     */
    String DEFAULT_STORAGE = "jooqDatabaseStorage";
    /**
     * Internal property to distinguish domain type objects.
     */
    String TYPE_PROPERTY = "_type";

    List<DomainProperty> getProperties();

    DomainProperty getProperty(String name);

    List<String> getPkFields();

    DomainService getDomainService();

    boolean isPKField(String name);

    String getStorageConfiguration();

    boolean isSystem();

    String getDescription();

    void postProcess(ApplicationModel applicationModel);
}

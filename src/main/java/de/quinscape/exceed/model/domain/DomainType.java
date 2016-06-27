package de.quinscape.exceed.model.domain;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.IncludeDocs;
import de.quinscape.exceed.runtime.domain.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DomainType
    extends TopLevelModel
{
    private final static Logger log = LoggerFactory.getLogger(DomainType.class);

    public final static Set<String> RESERVED_NAMES = ImmutableSet.of(
        DomainProperty.DATA_LIST_PROPERTY_TYPE,
        DomainProperty.DATA_LIST_ROOT_PROPERTY_TYPE,
        DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE
    );

    public final static String ID_PROPERTY = "id";

    private List<String> pkFields = Collections.singletonList(ID_PROPERTY);

    private Set<String> pkFieldSet = new HashSet<>(pkFields);

    /**
     * the linked hash map type ensures our properties stay in definition order
     */
    private List<DomainProperty> properties;

    private DomainService domainService;

    private String storageConfiguration = "jooqDatabaseStorage";


    @Override
    public void setName(String name)
    {
        if (RESERVED_NAMES.contains(name))
        {
            throw new IllegalArgumentException("'" + name + "' is a reserved domain object name.");
        }

        super.setName(name);
    }


    @JSONTypeHint(DomainProperty.class)
    public void setProperties(List<DomainProperty> properties)
    {
        this.properties = properties;
    }


    /**
     * List of properties for this domain type.
     * @return
     */
    @IncludeDocs
    public List<DomainProperty> getProperties()
    {
        return properties;
    }


    public void setDomainService(DomainService domainService)
    {
        this.domainService = domainService;
    }


    public void setPkFields(List<String> pkFields)
    {
        if (pkFields == null)
        {
            throw new IllegalArgumentException("pkFields can't be null");
        }

        this.pkFields = pkFields;
        this.pkFieldSet = new HashSet<>(pkFields);
    }


    /**
      List of properties that form the primary key for this type.
     */
    @JSONTypeHint(String.class)
    public List<String> getPkFields()
    {
        return pkFields;
    }

    /**
     * DomainService
     * @return
     */
    @JSONProperty(ignore = true)
    public DomainService getDomainService()
    {
        return domainService;
    }


    public boolean isPKField(String name)
    {
        return pkFieldSet.contains(name);
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "pkFields = " + pkFields
            + ", properties = " + properties
            ;
    }

    @PostConstruct
    public void init()
    {
        final String domainTypeName = getName();

        log.debug("Init {}", domainTypeName);

        for (DomainProperty property : properties)
        {
            property.setDomainType(domainTypeName);

            if (property.getType().equals("UUID"))
            {
                property.setRequired(true);
                if (property.getMaxLength() <= 0)
                {
                    property.setMaxLength(36);
                }
            }
        }
    }


    @JSONProperty(value = "storage", ignoreIfNull = true)
    public String getStorageConfiguration()
    {
        return storageConfiguration;
    }


    public void setStorageConfiguration(String storageConfiguration)
    {
        this.storageConfiguration = storageConfiguration;
    }
}

package de.quinscape.exceed.model.domain.type;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.domain.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an domain entity type within an exceed application.
 *
 * The persistent JSON output for this model generated by  {@link de.quinscape.exceed.runtime.model.ModelJSONServiceImpl#toJSON(Object)}.
 *
 */
public class DomainTypeModel
    extends AbstractTopLevelModel
    implements DomainType
{
    private final static Logger log = LoggerFactory.getLogger(DomainTypeModel.class);

    public final static Set<String> RESERVED_NAMES = ImmutableSet.of(
        PropertyType.DATA_GRAPH,
        PropertyType.DATA_LIST_ROOT_PROPERTY_TYPE,
        PropertyType.DOMAIN_TYPE
    );

    private List<String> pkFields = Collections.singletonList(ID_PROPERTY);

    private boolean system;

    private Set<String> pkFieldSet = new HashSet<>(pkFields);

    /**
     * the linked hash map type ensures our properties stay in definition order
     */
    private List<DomainProperty> properties;

    private DomainService domainService;

    private String description;

    private Map<String, DomainProperty> propertyMap;

    private String dataSource;


    /**
     * Name of the domain type. Should be start with an upper case letter.
     */
    @Override
    public String getName()
    {
        return super.getName();
    }


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

        if (properties != null)
        {
            final String domainTypeName = getName();

            log.debug("Init {}", domainTypeName);

            propertyMap = Maps.newHashMapWithExpectedSize(properties.size());

            for (DomainProperty property : properties)
            {
                property.setDomainType(domainTypeName);

                final String propertyName = property.getName();

                propertyMap.put(propertyName, property);
            }
        }

    }

    /**
     * List of properties for this domain type.
     *
     * @return
     */
    @Override
    @JSONProperty(priority = -40)
    public List<DomainProperty> getProperties()
    {
        return properties;
    }


    @Override
    public DomainProperty getProperty(String name)
    {
        return propertyMap.get(name);
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
     * List of properties that form the primary key for this type.
     */
    @Override
    @JSONTypeHint(String.class)
    @JSONProperty(priority = -10)
    public List<String> getPkFields()
    {
        return pkFields;
    }


    /**
     * DomainService
     *
     * @return
     */
    @Override
    @JSONProperty(ignore = true)
    public DomainService getDomainService()
    {
        return domainService;
    }


    @Override
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


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }

    @JSONProperty("dataSource")
    public void setDataSourceName(String dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * The exceed data source to use for this domain type. Default is "defaultDataSource"
     */
    @Override
    public String getDataSourceName()
    {
        return dataSource;
    }



    @Override
    @JSONProperty(priority = -30)
    @Internal
    public boolean isSystem()
    {
        return system;
    }


    public void setSystem(boolean system)
    {
        this.system = system;
    }

    @Override
    @JSONProperty(priority = 10, ignoreIfNull = true)
    public String getVersionGUID()
    {
        return super.getVersionGUID();
    }


    @Override
    public void setVersionGUID(String versionGUID)
    {
        super.setVersionGUID(versionGUID);
    }


    @Override
    @JSONProperty(priority = 20)
    public String getIdentityGUID()
    {
        return super.getIdentityGUID();
    }


    @Override
    public void setIdentityGUID(String identityGUID)
    {
        super.setIdentityGUID(identityGUID);
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public void postProcess(ApplicationModel applicationModel)
    {
        for (DomainProperty property : properties)
        {
            property.postProcess(applicationModel);
        }
    }

}

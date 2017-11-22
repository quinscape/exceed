package de.quinscape.exceed.model.domain.type;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.runtime.domain.DomainService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DomainTypeBuilder
{

    private final String name;

    private List<DomainProperty> properties;

    private List<String> pkFields = Arrays.asList(DomainType.ID_PROPERTY);

    private boolean system;

    private String version;

    private DomainService domainService;

    private String storageConfiguration;

    private String identityGUID;

    private String description;

    private String dataSourceName;


    public DomainTypeBuilder(String name)
    {
        this.name = name;
    }

    public DomainTypeBuilder withProperties(DomainProperty... properties)
    {
        this.properties = new ArrayList<>(Arrays.asList(properties));
        return this;
    }


    public DomainTypeBuilder withPkFields(List<String> pkFields)
    {
        this.pkFields = pkFields;
        return this;
    }


    public DomainTypeBuilder withSystem(boolean system)
    {
        this.system = system;
        return this;
    }


    public DomainTypeBuilder withVersion(String version)
    {
        this.version = version;
        return this;
    }


    public DomainTypeBuilder withDomainService(DomainService domainService)
    {
        this.domainService = domainService;
        return this;
    }


    public DomainTypeBuilder withStorageConfiguration(String storageConfiguration)
    {
        this.storageConfiguration = storageConfiguration;
        return this;
    }


    public DomainTypeBuilder withIdentityGUID(String identityGUID)
    {
        this.identityGUID = identityGUID;
        return this;
    }


    public DomainTypeBuilder withDescription(String description)
    {
        this.description = description;

        return this;
    }


    public DomainTypeBuilder withDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
        return this;
    }


    public DomainType build()
    {
        final DomainTypeModel domainType = new DomainTypeModel();
        domainType.setName(name);

        domainType.setProperties(properties);
        domainType.setPkFields(pkFields);
        domainType.setSystem(system);
        domainType.setVersionGUID(version);
        domainType.setDomainService(domainService);
        domainType.setIdentityGUID(identityGUID);
        domainType.setDescription(description);
        domainType.setDataSourceName(dataSourceName);

        return domainType;
    }


}

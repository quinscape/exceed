package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.EnumType;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates a domain state
 */
@Internal
public class DomainVersion
    extends AbstractTopLevelModel
{
    private String schema;

    private Map<String, DomainType> domainTypes;

    private Map<String, EnumType> enumTypes;

    private List<MigrationStepModel> migrationSteps;


    public void setSchema(String schema)
    {
        this.schema = schema;
    }


    public void setDomainTypes(Map<String, DomainType> domainTypes)
    {
        this.domainTypes = domainTypes;
    }


    public void setEnumTypes(Map<String, EnumType> enumTypes)
    {
        this.enumTypes = enumTypes;
    }


    public void setMigrationSteps(List<MigrationStepModel> migrationSteps)
    {
        this.migrationSteps = migrationSteps;
    }


    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypes;
    }


    public Map<String, EnumType> getEnumTypes()
    {
        return enumTypes;
    }


    public List<MigrationStepModel> getMigrationSteps()
    {
        return migrationSteps;
    }


    public String getSchema()
    {
        return schema;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }
}

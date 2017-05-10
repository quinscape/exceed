package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.Internal;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates a domain state
 */
public class DomainVersion
    extends TopLevelModel
{
    private final String schema;

    private final Map<String, DomainType> domainTypes;

    private final Map<String, EnumType> enumTypes;

    private final List<MigrationStepModel> migrationSteps;


    public DomainVersion(
        @JSONParameter("schema")
        String schema,
        @JSONParameter("domainTypes")
        @JSONTypeHint(DomainType.class)
        Map<String, DomainType> domainTypes,
        @JSONParameter("enumTypes")
        @JSONTypeHint(EnumType.class)
            Map<String, EnumType> enumTypes,
        @JSONParameter("migrationSteps")
        @JSONTypeHint(MigrationStepModel.class)
            List<MigrationStepModel>
            migrationSteps)
    {
        this.schema = schema;
        this.domainTypes = domainTypes;
        this.enumTypes = enumTypes;
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

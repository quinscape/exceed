package de.quinscape.exceed.model.staging;

import de.quinscape.exceed.model.AbstractModel;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONProperty;

/**
 * Abstract base model for the supported data source models.
 */
public abstract class AbstractDataSourceModel
    extends AbstractModel
    implements DataSourceModel
{
    private boolean shared;

    private String name;

    private SchemaUpdateMode schemaUpdateMode = SchemaUpdateMode.UPDATE;

    private String namingStrategyName;

    private String domainOperationsName;

    private String schemaServiceName;


    /**
     * The data source name, is automatically set when the data sources are added to the stage.
     *
     * @return
     */
    @Override
    @Internal
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Is <code>true</code> if the data source is available to all applications under its name. Each application needs
     * to define the shared data source with the same name. The actual model for the data source will be the merge result
     * of all definitions in system application order (i.e. in order of their position within startup.json / apps).
     *
     * @return <code>true</code> if shared
     */
    public boolean isShared()
    {
        return shared;
    }


    public void setShared(boolean shared)
    {
        this.shared = shared;
    }


    /**
     * Controls automatic schema updates for the data source.
     * @param schemaUpdateMode
     */
    @JSONProperty("schemaUpdate")
    public void setSchemaUpdateMode(SchemaUpdateMode schemaUpdateMode)
    {
        if (schemaUpdateMode == null)
        {
            throw new IllegalArgumentException("schemaUpdateMode can't be null");
        }

        this.schemaUpdateMode = schemaUpdateMode;
    }


    /**
     * The spring bean name of a {@link de.quinscape.exceed.runtime.domain.NamingStrategy} implementation.
     */
    @Override
    public String getNamingStrategyName()
    {
        return namingStrategyName;
    }


    @JSONProperty("namingStrategy")
    public void setNamingStrategyName(String namingStrategyName)
    {
        this.namingStrategyName = namingStrategyName;
    }


    /**
     * Spring bean name of a {@link de.quinscape.exceed.runtime.domain.DomainOperations} implementation.
     */
    @Override
    public String getDomainOperationsName()
    {
        return domainOperationsName;
    }


    @JSONProperty("domainOperations")
    public void setDomainOperationsName(String domainOperationsName)
    {
        this.domainOperationsName = domainOperationsName;
    }


    /**
     * Spring bean name of a {@link de.quinscape.exceed.runtime.schema.SchemaService} implementation. The system provides
     * <code>"defaultSchemaService"</code> and <code>"noopSchemaService"</code>
     */
    @Override
    public String getSchemaServiceName()
    {
        return schemaServiceName;
    }


    @JSONProperty("schemaService")
    public void setSchemaServiceName(String schemaServiceName)
    {
        this.schemaServiceName = schemaServiceName;
    }


    @Override
    public SchemaUpdateMode getSchemaUpdateMode()
    {
        return schemaUpdateMode;
    }

    @Override
    public void validate()
    {
        if (getNamingStrategyName() == null)
        {
            throw new InconsistentModelException("Data source model '" + name + " defines no namingStrategy");
        }
//        if (getDomainOperationsName() == null)
//        {
//            throw new InconsistentModelException("Data source model '" + name + " defines no domainOperationsName");
//        }
        if (getSchemaServiceName() == null)
        {
            throw new InconsistentModelException("Data source model '" + name + " defines no schemaService");
        }
    }
}

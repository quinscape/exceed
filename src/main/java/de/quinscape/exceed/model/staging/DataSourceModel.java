package de.quinscape.exceed.model.staging;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.annotation.Internal;

public interface DataSourceModel
    extends Model
{
    boolean isShared();

    boolean isPrimary();
    
    String getName();

    String getNamingStrategyName();

    String getDomainOperationsName();

    String getSchemaServiceName();

    SchemaUpdateMode getSchemaUpdateMode();

    /**
     * Returns the name of the factory implementation that converts models of this type to an actual data source.
     */
    String getDataSourceFactoryName();

    void setName(String name);

    void validate();
}

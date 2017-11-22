package de.quinscape.exceed.model.staging;


import de.quinscape.exceed.model.annotation.Internal;

/**
 * A JOOQ-based SQL data source.
 */
public class SystemDataSourceModel
    extends AbstractDataSourceModel
{
    @Override
    public boolean isPrimary()
    {
        return false;
    }


    @Override
    @Internal
    public String getDataSourceFactoryName()
    {
        return "systemDataSourceModelFactory";
    }
}

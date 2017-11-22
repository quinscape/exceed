package de.quinscape.exceed.runtime.datasrc;

public interface DataSourceResolver
{
    ExceedDataSource resolve(String dataSourceName);
}

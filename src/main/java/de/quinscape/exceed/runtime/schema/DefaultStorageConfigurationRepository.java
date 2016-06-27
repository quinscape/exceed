package de.quinscape.exceed.runtime.schema;

import java.util.Map;

public class DefaultStorageConfigurationRepository
    implements StorageConfigurationRepository
{
    private final Map<String, StorageConfiguration> configurations;


    public DefaultStorageConfigurationRepository(Map<String, StorageConfiguration> configurations)
    {
        this.configurations = configurations;
    }

    @Override
    public StorageConfiguration getConfiguration(String name)
    {
        final StorageConfiguration storageConfiguration = configurations.get(name);

        if (storageConfiguration == null)
        {
            throw new IllegalStateException("Storage configuration '" + name + "' not found.");
        }

        return storageConfiguration;
    }
}

package de.quinscape.exceed.runtime.schema;

import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

public class DefaultStorageConfigurationRepository
    implements StorageConfigurationRepository
{
    private final Map<String, StorageConfiguration> configurations;
    private final Set<String> names;

    private final String defaultStorage;


    public DefaultStorageConfigurationRepository(Map<String, StorageConfiguration> configurations, String defaultStorage)
    {
        this.configurations = configurations;
        this.defaultStorage = defaultStorage;

        this.names = ImmutableSet.copyOf(configurations.keySet());
    }


    @Override
    public Set<String> getConfigurationNames()
    {
        return names;
    }


    @Override
    public StorageConfiguration getConfiguration(String name)
    {

        if (name == null)
        {
            name = defaultStorage;
        }


        final StorageConfiguration storageConfiguration = configurations.get(name);

        if (storageConfiguration == null)
        {
            throw new IllegalStateException("Storage configuration '" + name + "' not found.");
        }

        return storageConfiguration;
    }
}

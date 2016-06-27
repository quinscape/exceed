package de.quinscape.exceed.runtime.schema;

/**
 * Provides access to the configured {@link DefaultStorageConfiguration} instances in the system by their logical name.
 */
public interface StorageConfigurationRepository
{
    StorageConfiguration getConfiguration(String name);
}

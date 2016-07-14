package de.quinscape.exceed.runtime.schema;

import java.util.Set;

/**
 * Provides access to the configured {@link DefaultStorageConfiguration} instances in the system by their logical name.
 */
public interface StorageConfigurationRepository
{
    Set<String> getConfigurationNames();

    StorageConfiguration getConfiguration(String name);
}

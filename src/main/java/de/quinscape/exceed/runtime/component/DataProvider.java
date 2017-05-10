package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.view.DataProviderContext;

import java.util.Map;

/**
 * Implemented by classes that provide data for component instances at runtime.
 *
 *
 * @see ComponentDescriptor#getDataProvider()
 */
public interface DataProvider
{
    /**
     * Provides data for the given data provider context and element node.
     *
     * @param dataProviderContext   data provider context
     * @param componentModel        element node
     * @param vars
     * @return
     */
    Map<String,Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel, Map<String,
        Object> vars);
}

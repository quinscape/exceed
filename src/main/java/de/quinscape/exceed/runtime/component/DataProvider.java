package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.view.DataProviderContext;

import java.util.Map;

public interface DataProvider
{
    /**
     * Provides data for the given data provider context and element node.
     *
     * @param dataProviderContext   data provider context
     * @param componentModel           element node
     * @return
     */
    Map<String,Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel);
}

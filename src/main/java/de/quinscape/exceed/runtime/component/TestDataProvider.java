package de.quinscape.exceed.runtime.component;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.view.DataProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TestDataProvider
    implements DataProvider
{
    private final static Logger log = LoggerFactory.getLogger(TestDataProvider.class);


    @Override
    public Map<String, Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel,
                                       Map<String, Object> vars)
    {
        log.info("vars: {}", vars);

        return ImmutableMap.of("injection", "injection value");
    }
}

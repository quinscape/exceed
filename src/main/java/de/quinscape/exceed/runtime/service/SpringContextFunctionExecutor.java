package de.quinscape.exceed.runtime.service;

import de.quinscape.dss.DSSFunction;
import de.quinscape.dss.DynamicStylesheetsException;
import de.quinscape.dss.FunctionContext;
import de.quinscape.dss.FunctionExecutor;
import de.quinscape.dss.value.DSSValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SpringContextFunctionExecutor
    implements FunctionExecutor
{
    private final static Logger log = LoggerFactory.getLogger(SpringContextFunctionExecutor.class);

    private static final String DSS_PREFIX = "dss_";

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, DSSFunction> functions;


    @Override
    public boolean isUserDefinedFunction(String name)
    {
        return functions.containsKey(name);
    }

    @Override
    public DSSValue executeFunction(FunctionContext functionContext, List<? extends DSSValue> list) throws DynamicStylesheetsException
    {
        final DSSFunction fn = functions.get(functionContext.getFunctionName());

        if (fn == null)
        {
            throw new IllegalStateException("No function '" + functionContext.getFunctionName() + "'");
        }
        return fn.execute(functionContext, list);
    }

    @Override
    public void flushCaches()
    {
        final Map<String, DSSFunction> beansOfType = applicationContext.getBeansOfType(DSSFunction.class);

        functions = new HashMap<>();

        for (Map.Entry<String, DSSFunction> entry : beansOfType.entrySet())
        {
            String name = entry.getKey();
            if (name.startsWith(DSS_PREFIX))
            {
                name = name.substring(DSS_PREFIX.length());
            }

            functions.put(name, entry.getValue());
        }
        log.debug("DSS functions: {}", functions.keySet());
    }

    @PostConstruct
    public void init()
    {
        flushCaches();
    }
}

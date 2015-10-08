package de.quinscape.exceed.runtime.service;

import de.quinscape.dss.DSSFunction;
import de.quinscape.dss.DynamicStylesheetsException;
import de.quinscape.dss.FunctionContext;
import de.quinscape.dss.FunctionExecutor;
import de.quinscape.dss.value.DSSValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SpringContextFunctionExecutor
    implements FunctionExecutor
{
    @Autowired
    private ApplicationContext applicationContext;
    private Set<String> functionNames;


    @Override
    public boolean isUserDefinedFunction(String s)
    {
        return functionNames.contains(s);
    }

    @Override
    public DSSValue executeFunction(FunctionContext functionContext, List<? extends DSSValue> list) throws DynamicStylesheetsException
    {
        DSSFunction bean = (DSSFunction) applicationContext.getBean(functionContext.getFunctionName());
        return bean.execute(functionContext, list);
    }

    @Override
    public void flushCaches()
    {
        functionNames = new HashSet<>();
        Collections.addAll(functionNames, applicationContext.getBeanNamesForType(DSSFunction.class));

    }

    @PostConstruct
    public void init()
    {
        flushCaches();
    }
}

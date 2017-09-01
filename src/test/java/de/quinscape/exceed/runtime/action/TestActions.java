package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.annotation.ExceedPropertyType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

public class TestActions
{
    private final static Logger log = LoggerFactory.getLogger(TestActions.class);

    private MethodCall lastArgs;


    public MethodCall getLastArgs()
    {
        return lastArgs;
    }


    @Action
    public boolean method(RuntimeContext runtimeContext, int parameter)
    {
        register("method", runtimeContext, parameter);
        return parameter == 1;
    }

    @Action
    public boolean dateParam(RuntimeContext runtimeContext, Date date)
    {
        register("dateParam", runtimeContext, date);
        return true;
    }

    @Action
    public String requiredParam(RuntimeContext runtimeContext, @ExceedPropertyType(type = PropertyType.PLAIN_TEXT, required = true)  String required)
    {
        return required;
    }
    
    @Action
    @ExceedPropertyType(type = PropertyType.DOMAIN_TYPE, typeParam = "Foo")
    public DomainObject domainObjectParam(@ExceedPropertyType(type = PropertyType.DOMAIN_TYPE, typeParam = "Foo") DomainObject domainObject)
    {
        if (domainObject != null)
        {
            domainObject.setProperty("foo", new Date(TimeUnit.DAYS.toMillis(3)));
        }
        return domainObject;
    }

    @Action
    public Bar modeledDomain(Bar bar)
    {
        return bar;
    }

    @Action
    public Date dateBack()
    {
        return new Date(TimeUnit.DAYS.toMillis(2));
    }

    @Action
    public void voidMethod()
    {
    }

    @Action
    public void varArgsMethod(Object... values)
    {
        register("varArgsMethod", values);
    }
    
    @Action
    public void varArgsMethod2(String name, Object... values)
    {
        register("varArgsMethod", name, values);
    }

    @Action
    public String scopedValue(@ExceedContext("sessionVar") String scoped)
    {
        return scoped;
    }

    @Action
    public String requiredScopedValue(

        @ExceedPropertyType(type = PropertyType.PLAIN_TEXT, required = true)
        @ExceedContext("nullVar")
        String scoped

    )
    {
        return scoped;
    }


    private void register(String method, Object... args)
    {
        lastArgs = new MethodCall(method, args);
    }
}

Exceed Action System
====================

The action system of exceed allows the execution of Java methods within the context of
an exceed application. This is used to implement basic exceed functionality and 
can and should be used for application-specific extension.

The exceed expression language is designed to not be overly complex, at most 
action expression chains with conditionals. For anything more complex you should
move that into an action / components / other extension points. 


Using action annotations
------------------------

The action system works with a set of annotations on classes and methods. We define
a spring meta annotation @CustomLogic. Using that annotation on a class will make spring
automatically detect our class in component scans.

The custom logic classes can be spring bean autowiring targets just like any other spring
service.

The @Action annotation marks java methods within @CustomLogic annoated classes as 
being action methods. Here we see an example of such a method 

```
@CustomLogic
public class ApplicationSpecificLogic
{
    @Action
    public boolean myBusinessAction(
        RuntimeContext runtimeContext,
        int number
    )
    {
        …
    }
}
```

This action method then can be accessed inside an action expression (for example a transition action expression).


```
    myBusinessAction(123)
```

The action methods can receive parameters that are either provided by the system or
by the action usage in an expression.

The runtime context parameter for example automatically provides access to the context of the 
application the action is happening in.

Currently the system by default automatically provides:

 * RuntimeContext 
 * Parameters annotated with @ExceedContext receive the current value of a context variable 

The rest of the parameters for which no special parameter providers exist make up
the formal parameters of the action expression function corresponding to the 
annotated java method.

In our example case this is the parameter *number*.

The parameters are subject to normal exceed parameter conversion rules. You can
use the @ExceedPropertyType annotation to specify a property type in cases where
the property type does not follow out of the Java parameter type.

Examples:

```
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import com.myapp.domain.tables.pojos.Bar;

    ...
    
    @Action
    public boolean myBusinessAction(
        @ExceedPropertyType(type="DomainType", typeParam="Foo") GenericDomainObject fooObject,
        Bar barObject
    )
    {
        …
    }
```

Here, the *fooObject* parameter is a GenericDomainObject, which could contain any domain 
object value, but the annotation makes it clear that it is supposed to receive
a domain object of the type *Foo*.

If you use JOOQ class generation with exceed's de.quinscape.exceed.tooling.GeneratorStrategy,
it will create POJO implementations extending from de.quinscape.exceed.runtime.domain.GeneratedDomainObject.

These domain object implementations can be used as action parameters without any
further annotations, their class name is identical to the domain type name.

Extending Action Service Parameter Handling
-------------------------------------------

The action service uses implementations of ParameterProviderFactory interface
to create parameter providers to provide such parameter values.
 
```
import  de.quinscape.exceed.runtime.action.ParameterProviderFactory;

public interface ParameterProviderFactory
{
    ParameterProvider createIfApplicable(Class<?> parameterClass, Annotation[] annotations) throws Exception;
}
```

The factory returns a parameter provider if it can construct one for the given parameter
type and annotations on that parameter.

All Spring beans in the exceed contexts implementing this interface will be automatically considered as provider factories.

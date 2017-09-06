package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the registration of a component definition and contains the component descriptor, the processed styles
 * for the component and the data provider implementation.
 */
public class ComponentRegistration
{
    private final ComponentDescriptor descriptor;

    private final String styles;

    private final String componentName;

    private final DataProvider dataProvider;

    private final String moduleName;

    private final QueryTransformer queryTransformer;

    private volatile Map<String,ExpressionValue> queryExpressions;

    public ComponentRegistration(
        String componentName,
        ComponentDescriptor descriptor,
        String styles,
        DataProvider dataProvider,
        String moduleName,
        QueryTransformer queryTransformer
    )
    {
        this.moduleName = moduleName;
        if (componentName == null)
        {
            throw new IllegalArgumentException("componentName can't be null");
        }

        if (descriptor == null)
        {
            throw new IllegalArgumentException("descriptor can't be null");
        }

        this.componentName = componentName;
        this.descriptor = descriptor;
        this.styles = styles;
        this.dataProvider = dataProvider;
        this.queryTransformer = queryTransformer;
    }

    public Map<String, ExpressionValue> getQueryExpressions(RuntimeContext runtimeContext)
    {
        if (queryExpressions == null)
        {
            synchronized (this)
            {
                if (queryExpressions == null)
                {
                    queryExpressions = new HashMap<>();

                    final Map<String, Object> queries = descriptor.getQueries();

                    for (Map.Entry<String, Object> entry : queries.entrySet())
                    {
                        final String name = entry.getKey();
                        final Object value = entry.getValue();

                        if (!(value instanceof String))
                        {
                            throw new QueryPreparationException("Query object " + value + " is in valid for " + queryTransformer);
                        }
                        queryExpressions.put(name, ExpressionValue.forValue((String) value, true));
                    }
                }
            }
        }
        return queryExpressions;
    }

    public ComponentDescriptor getDescriptor()
    {
        return descriptor;
    }


    public String getComponentName()
    {
        return componentName;
    }


    public String getStyles()
    {
        return styles;
    }


    public DataProvider getDataProvider()
    {
        return dataProvider;
    }


    public String getModuleName()
    {
        return moduleName;
    }


    public QueryTransformer getQueryTransformer()
    {
        return queryTransformer;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "componentName = '" + componentName + '\''
            + ", dataProvider = " + dataProvider
            ;
    }
}



package de.quinscape.exceed.runtime.action.param;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionUtil;
import de.quinscape.exceed.runtime.action.ExceedQualifier;
import de.quinscape.exceed.runtime.action.ParameterProvider;
import de.quinscape.exceed.runtime.action.ParameterProviderFactory;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import org.jooq.DSLContext;

import java.lang.annotation.Annotation;

public class DataSourceProviderFactory
    implements ParameterProviderFactory, ParameterProvider
{
    @Override
    public ParameterProvider createIfApplicable(Class<?> parameterClass, Annotation[] annotations)
    {
        final boolean isDSLContext = parameterClass.equals(DSLContext.class);
        if (isDSLContext || parameterClass.equals(ExceedDataSource.class))
        {
            final ExceedQualifier qualifier = ActionUtil.find(annotations, ExceedQualifier.class);
            final String dataSourceName;

            if (qualifier != null && qualifier.value().length() > 0)
            {
                dataSourceName = qualifier.value();
            }
            else
            {
                // default data source
                dataSourceName = null;
            }

            return new DataSourceProvider(dataSourceName, isDSLContext);
        }
        else
        {
            return null;
        }
    }

    @Override
    public RuntimeContext provide(RuntimeContext runtimeContext)
    {
        return runtimeContext;
    }
}

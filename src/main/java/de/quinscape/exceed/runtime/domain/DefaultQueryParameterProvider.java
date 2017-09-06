package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.QueryPreparationException;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;

import java.util.Map;

public class DefaultQueryParameterProvider
    implements QueryTypeParameterProvider
{
    public static final String PROPERTY_NAME_OFFSET = "offset";

    public static final String PROPERTY_NAME_LIMIT = "limit";

    @Override
    public Object[] getSqlParameters(
        RuntimeContext runtimeContext,
        QueryDefinition queryDefinition
    )
    {
        final Map<String, Object> values = queryDefinition.getParameters();

        final QueryTypeModel queryTypeModel = QueryTypeModel.from(queryDefinition);

        final Map<String, DomainProperty> parameterMap = queryTypeModel.getParameterMap();
        final Object[] paramArray = new Object[parameterMap.size()];

        for (DomainProperty domainProperty : parameterMap.values())
        {
            final String name = domainProperty.getName();
            int index = (int) domainProperty.getConfig().get(QueryTypeModel.SQL_PARAM_INDEX);
            Object value = values.get(name);
            if (value == null)
            {
                final ASTExpression defaultValueExpression = domainProperty.getDefaultValueExpression();
                if (defaultValueExpression != null)
                {
                    value = runtimeContext.getJsEnvironment().getValue(runtimeContext,
                        defaultValueExpression);
                }

                if (value == null)
                {
                    if (name.equals(PROPERTY_NAME_LIMIT))
                    {
                        value = queryDefinition.getLimit();
                    }
                    else if (name.equals(PROPERTY_NAME_OFFSET))
                    {
                        value = queryDefinition.getOffset();
                    }

                    if (domainProperty.isRequired())
                    {
                        throw new QueryPreparationException("Required Sql Parameter '" + name + "' is missing.");
                    }
                }
            }
            paramArray[index] = value;
        }

        return paramArray;
    }
}


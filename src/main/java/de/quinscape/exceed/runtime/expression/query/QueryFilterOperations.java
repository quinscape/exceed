package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import de.quinscape.exceed.runtime.expression.annotation.OperationParam;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExpressionOperations(environment = QueryFilterEnvironment.class)
public class QueryFilterOperations
{
    private static Logger log = LoggerFactory.getLogger(QueryFilterOperations.class);

    /**
     * Returns a component attribute value. Only valid in component contexts.
     */
    @Operation(
        params = {
            @OperationParam(type = "String", name = "name", description = "Component attribute to read")
        }
    )
    public Condition prop(ExpressionContext<QueryFilterEnvironment> ctx, String name)
    {
        QueryTransformerEnvironment tEnv = ctx.getEnv().getTransformerEnvironment();

        ComponentModel componentModel = tEnv.getComponentModel();
        ExpressionValue attributeValue = componentModel.getAttribute(name);

        if (attributeValue == null)
        {
            ExpressionValue defaultValue = componentModel.getComponentRegistration().getDescriptor().getPropTypes()
                .get(name).getDefaultValue();

            ASTExpression astExpression;
            if (defaultValue != null && (astExpression = defaultValue.getAstExpression()) != null)
            {
                return (Condition) astExpression.jjtAccept(ctx.getEnv(), null);
            }
            return null;
        }

        ASTExpression astExpression = attributeValue.getAstExpression();
        if (astExpression == null)
        {
            throw new QueryTransformationException("Value of prop imported into filter must be expression: " + componentModel);
        }

        return (Condition) astExpression.jjtAccept(ctx.getEnv(), null);
    }

    /**
     * Returns a new condition combining child filter values. This is used e.g. to combine all the user-input controlled
     * filters of a data grid columns.
     */
    @Operation
    public Condition combineChildFilters(ExpressionContext<QueryFilterEnvironment> ctx)
    {
        QueryTransformerEnvironment env = ctx.getEnv().getTransformerEnvironment();

        final ComponentModel componentModel = env.getComponentModel();


        List<ComponentModel> children = componentModel.children();
        int size = children.size();

        List<Condition> conditions = new ArrayList<>();
        if (size > 0)
        {
            for (int i = size - 1; i >= 0; i--)
            {
                ComponentModel kid = children.get(i);
                Condition filter = getFilter(ctx, kid);
                if (filter != null)
                {
                    conditions.add(filter);
                }
            }
        }

        if (conditions.size() == 0)
        {
            return null;
        }
        else if (conditions.size() == 1)
        {
            return conditions.get(0);
        }
        else
        {
            final Object filter = ctx.getEnv().getTransformerEnvironment().getVar("filter");


            if (filter instanceof String)
            {
                // we either have a string which we compare with each column and OR those results or..
                return DSL.or(conditions);
            }
            else
            {
                // .. we either have a map with match values for each column and we AND the results.
                return DSL.and(conditions);
            }
        }
    }


    /**
     * Resolves the field with the given name. This is equivalent to just using the field name as identifier.
     * 
     * <pre class="text-info"> field('name') == 0 </pre>
     *
     * is the same as
     *
     * <pre class="text-info"> name == 0 </pre>
     *
     */
    @Operation(
        params = {
            @OperationParam(type = "String", name = "name", description = "Field name")
        }
    )
    public Field field(ExpressionContext<QueryFilterEnvironment> ctx)
    {
        QueryFilterEnvironment env = ctx.getEnv();
        ComponentModel columnContext = env.getColumnContext();

        String name;
        ASTFunction fnNode = ctx.getASTFunction();
        if (fnNode.jjtGetNumChildren() == 0)
        {
            name = columnContext.getAttribute("name").getValue();
        }
        else
        {
            name = (String) fnNode.jjtGetChild(0).jjtAccept(env, null);
        }


        final StorageConfigurationRepository storageConfigurationRepository = env.getTransformerEnvironment()
            .getStorageConfigurationRepository();

        QueryDomainType queryDomainType = ctx.getEnv().getQueryDomainType();
        DataField dataField = queryDomainType.resolveField(name);
        NamingStrategy namingStrategy =  storageConfigurationRepository.getConfiguration(queryDomainType.getType().getStorageConfiguration()).getNamingStrategy();

        return DSL.field(DSL.name(dataField.getNameFromStrategy(namingStrategy)));
    }


    /**
     * Returns the current filter value for the current column/field component context. Useful for component filters or
     * filter templates.
     */
    @Operation
    public Field filterValue(ExpressionContext<QueryFilterEnvironment> ctx)
    {
        QueryFilterEnvironment env = ctx.getEnv();
        ComponentModel columnContext = env.getColumnContext();

        ASTFunction fnNode = ctx.getASTFunction();
        String name;
        if (fnNode.jjtGetNumChildren() == 0)
        {
            name = columnContext.getAttribute("name").getValue();
        }
        else
        {
            name = (String) fnNode.jjtGetChild(0).jjtAccept(env, null);
        }
        String filterValue = getFilterValue(ctx, name);

        log.debug("Filter value for '{}' is {}", name, filterValue);

        return DSL.value(filterValue);
    }


    private Condition getFilter(ExpressionContext<QueryFilterEnvironment> ctx, ComponentModel kid)
    {
        Node expr = null;
        ExpressionValue attrVal = kid.getAttribute("filterTemplate");
        if (attrVal == null)
        {
            final ComponentInstanceRegistration componentRegistration = kid.getComponentRegistration();
            if (componentRegistration != null)
            {
                PropDeclaration filterTemplateProp = componentRegistration.getDescriptor().getPropTypes().get("filterTemplate");

                if (filterTemplateProp != null)
                {
                    ExpressionValue defaultValue = filterTemplateProp.getDefaultValue();

                    if (defaultValue != null)
                    {

                        ASTExpression defaultExpr = defaultValue.getAstExpression();
                        if (defaultExpr == null)
                        {
                            throw new QueryTransformationException("Filter-template default is no expression for " +
                                "component '" + kid.getName() + "'");
                        }

                        expr = defaultExpr;
                    }
                }
            }
        }
        else
        {
            ASTExpression astExpression = attrVal.getAstExpression();
            if (astExpression == null)
            {
                throw new QueryTransformationException("Filter must be expression: " + kid);
            }
            expr = astExpression;
        }

        if (expr == null)
        {
            return null;
        }

        QueryFilterEnvironment filterEnv = ctx.getEnv();

        filterEnv.setColumnContext(kid);
        try
        {
            String filterValue = getFilterValue(ctx, kid.getAttribute("name").getValue());

            if (StringUtils.hasText(filterValue))
            {
                return (Condition) expr.jjtAccept(filterEnv, null);
            }
            else
            {
                return null;
            }
        }
        finally
        {
            filterEnv.setColumnContext(null);
        }
    }


    private String getFilterValue(ExpressionContext<QueryFilterEnvironment> ctx, String name)
    {
        QueryFilterEnvironment filterEnv = ctx.getEnv();
        Object value = filterEnv.getTransformerEnvironment().getVar("filter");

        if (value == null)
        {
            return null;
        }

        if (value instanceof String)
        {
            return (String) value;
        }

        if (!(value instanceof Map))
        {
            throw new QueryTransformationException("Filter variable must be a map or string (is " + value + "): " + filterEnv.getTransformerEnvironment().getComponentModel());

        }
        return ((Map<String, String>) value).get(name);
    }
}

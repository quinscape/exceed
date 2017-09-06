package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.component.ComponentClasses;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.context.ScopeDeclaration;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionResult;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.jooq.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@ExpressionOperations(environment = QueryTransformerEnvironment.class)
public class QueryTransformerOperations
{

    private final static Logger log = LoggerFactory.getLogger(QueryTransformerOperations.class);

    public static final String THEN = "then";

    private static final String DATA_CURSOR = "DataCursor";

    private static final String DATA_ATTRIBUTE = "data";

    private static final String VALUE_ATTRIBUTE = "value";

    private ExpressionService expressionService;


    public void setExpressionService(ExpressionService expressionService)
    {
        this.expressionService = expressionService;
    }

    @Operation
    public Object param(ExpressionContext<QueryTransformerEnvironment> ctx, String name)
    {
        return ctx.getEnv().getRuntimeContext().getLocationParams().get(name);
    }

    @Operation
    public Object var(ExpressionContext<QueryTransformerEnvironment> ctx, String name)
    {
        return ctx.getEnv().getVar(name);
    }

    @Operation
    public Object prop(ExpressionContext<QueryTransformerEnvironment> ctx, String name)
    {
        if (ctx.getEnv().getComponentModel() == null)
        {
            throw new IllegalStateException("prop() only valid in component expressions: " + ExpressionUtil.renderExpressionOf(ctx.getASTFunction()));
        }

        QueryTransformerEnvironment env = ctx.getEnv();
        final ComponentModel componentModel = env.getComponentModel();
        ExpressionValue attribute = componentModel.getAttribute(name);
        if (attribute == null)
        {
            final PropDeclaration propDecl = componentModel.getComponentRegistration().getDescriptor()
                .getPropTypes().get(name);
            if (propDecl != null)
            {
                final ExpressionValue defaultValue = propDecl.getDefaultValue();
                if (defaultValue != null && defaultValue.getAstExpression() != null)
                {
                    return defaultValue.getAstExpression().jjtAccept(env, null);
                }
            }


            return null;

        }
        ASTExpression expression = attribute.getAstExpression();
        if (expression != null)
        {
            return expression.jjtAccept(env, null);
        }
        else
        {
            return attribute.getValue();
        }
    }

    @Operation
    public List<String> childFieldRefs(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        final ComponentModel componentModel = ctx.getEnv().getComponentModel();
        Set<String> names = new LinkedHashSet<>();
        findFieldRefs(names, componentModel);
        return new ArrayList<>(names);

    }


    private void findFieldRefs(Set<String> names, ComponentModel componentModel)
    {
        final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
        if (registration != null)
        {
            final Map<String, PropDeclaration> propTypes = registration.getDescriptor().getPropTypes();
            if (propTypes != null)
            {
                for (Map.Entry<String, PropDeclaration> e : propTypes.entrySet())
                {
                    final String attrName = e.getKey();
                    final PropDeclaration propDeclaration = e.getValue();

                    if (propDeclaration.getType() == PropType.FIELD_REFERENCE)
                    {
                        final String name = componentModel.getAttribute(attrName).getValue();
                        if (!names.contains(name))
                        {
                            names.add(name);
                        }
                    }
                }
            }
        }


        for (ComponentModel kid : componentModel.children())
        {
            findFieldRefs(names, kid);
        }
    }


//    public List<String> childAttrs(ExpressionContext<QueryTransformerEnvironment> ctx)
//    {
//        final ASTFunction fn = ctx.getASTFunction();
//
//        String attrName;
//        if (fn.jjtGetNumChildren() == 1)
//        {
//            attrName = (String) fn.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);
//        }
//        else
//        {
//            attrName = "name";
//        }
//
//        QueryTransformerEnvironment env = ctx.getEnv();
//        List<String> names = new ArrayList<>();
//        for (ComponentModel kid : env.getComponentModel().children())
//        {
//            ExpressionValue attrVal = kid.getAttribute(attrName);
//            if (attrVal != null)
//            {
//                names.add(attrVal.getValue());
//            }
//        }
//        return names;
//    }
//
//
//    @Operation
//    public List<String> childAttributes(ExpressionContext<QueryTransformerEnvironment> ctx, String cls, String attr)
//    {
//        if (attr == null)
//        {
//            attr = "name";
//        }
//        List<String> values = new ArrayList<>();
//        attributeValueForClass(values, ctx.getEnv().getComponentModel(), cls, attr);
//        return values;
//
//    }

    @Operation
    public String appName(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        return ctx.getEnv().getRuntimeContext().getApplicationModel().getName();
    }


    private void attributeValueForClass(List<String> values, ComponentModel componentModel, String cls, String attr)
    {
        ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();

        if (componentRegistration != null)
        {
            if (componentRegistration.getDescriptor().getClasses().contains(cls))
            {
                ExpressionValue attribute = componentModel.getAttribute(attr);
                if (attribute != null)
                {
                    values.add(attribute.getValue());
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                attributeValueForClass(values, kid, cls, attr);
            }
        }
    }

    @Operation(context = QueryDomainType.class)
    public QueryDefinition query(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return new QueryDefinition(queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition join(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition crossJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition fullOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition leftOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition rightOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition naturalJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition naturalLeftOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }

    @Operation(context = QueryDomainType.class)
    public JoinDefinition naturalRightOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    private JoinDefinition joinInternal(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        ASTFunction node = ctx.getASTFunction();
        Object result = ExpressionUtil.visitOneChildOf(ctx.getEnv(), node, ASTFunction.class, ASTPropertyChain.class, ASTIdentifier.class);
        String joinName = node.getName();
        if (result instanceof QueryDomainType)
        {
            QueryDomainType other = (QueryDomainType) result;

            final JoinDefinition joinedType = queryDomainType.getJoinedType();
            if (joinedType != null)
            {
                return joinedType.getRight().join(joinName, other);
            }
            else
            {
                return queryDomainType.join(joinName, other);
            }
        }
        else
        {
            throw new QueryTransformationException("Invalid argument for " + joinName + "(): " + result + " ( was:" + node + ")");
        }
    }


    @Operation(context = JoinDefinition.class)
    public QueryDomainType on(ExpressionContext<QueryTransformerEnvironment> ctx, JoinDefinition joinDefinition)
    {
        SimpleNode n = ExpressionUtil.expectChildOf(ctx.getASTFunction(), ASTEquality.class, ASTRelational.class, ASTLogicalAnd
            .class, ASTLogicalOr.class, ASTExpression.class);

        QueryCondition condition = transformFilter(ctx.getEnv(), joinDefinition.getLeft(), n);

        joinDefinition.setCondition(condition);
        return joinDefinition.getLeft();
    }

    @Operation(context = QueryDomainType.class)
    public QueryDomainType fields(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        List<String> fields = new ArrayList<>();

        ASTFunction node = ctx.getASTFunction();

        int numKids = node.jjtGetNumChildren();
        for (int i = 0; i < numKids; i++)
        {
            Node kid = node.jjtGetChild(i);

            Object result = kid.jjtAccept(ctx.getEnv(), null);
            if (result instanceof List)
            {
                fields.addAll((List)result);
            }
            else if (result instanceof String)
            {
                fields.add((String) result);
            }
        }

        final ComponentModel componentModel = ctx.getEnv().getComponentModel();
        if (componentModel != null && componentModel.getComponentRegistration().getDescriptor().hasClass(ComponentClasses.QUERY_IDS))
        {
            fields.add("id");
        }

        queryDomainType.selectedFields(fields);
        return queryDomainType;
    }

    @Operation(context = QueryDomainType.class)
    public QueryDomainType as(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        ASTFunction node = ctx.getASTFunction();
        Node arg = node.jjtGetNumChildren() > 0 ? node.jjtGetChild(0) : null;
        String alias;
        if (arg instanceof ASTString)
        {
            alias = ((ASTString) arg).getValue();
        }
        else if (arg instanceof ASTIdentifier)
        {
            alias = ((ASTIdentifier) arg).getName();
        }
        else
        {
            throw new QueryTransformationException("Invalid arg for as(): " + arg);
        }
        queryDomainType.setAlias(alias);
        return queryDomainType;
    }


    @Operation(context = QueryDefinition.class)
    public QueryDefinition limit(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();

        ASTFunction node = ctx.getASTFunction();

        int len = node.jjtGetNumChildren();

        Integer limit;
        Node n;
        if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
        {
            final Number limitVal = (Number) n.jjtAccept(env, null);
            limit = limitVal != null ? limitVal.intValue() : null;
        }
        else
        {
            final Number limitVal = (Number) ExpressionUtil.visitOneChildOf(env, ctx.getASTFunction(), ASTInteger.class);
            limit = limitVal != null ? limitVal.intValue() : null;
        }

        if ( limit != null)
        {
            queryDefinition.setLimit(limit);
        }
        return queryDefinition;
    }


    @Operation(context = QueryDefinition.class)
    public QueryDefinition offset(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        ASTFunction node = ctx.getASTFunction();
        int len = node.jjtGetNumChildren();

        Integer offset;
        Node n;
        if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
        {
            offset = ((Number) n.jjtAccept(env, null)).intValue();
        }
        else
        {
            offset = (Integer) ExpressionUtil.visitOneChildOf(env, node, ASTInteger.class);
        }

        if ( offset != null)
        {
            queryDefinition.setOffset(offset);
        }
        return queryDefinition;
    }


    @Operation(context = QueryDefinition.class)
    public QueryDefinition orderBy(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        ASTFunction node = ctx.getASTFunction();
        int len = node.jjtGetNumChildren();

        List<String> fields;

        Node n;
        if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
        {
            fields = (List<String>) n.jjtAccept(env, null);
        }
        else
        {
            fields = new ArrayList<>(len);


            for (int i = 0; i < len; i++)
            {

                n = node.jjtGetChild(i);
                fields.add(evaluateOrderByArg(n));
            }
        }

        if (fields != null)
        {
            queryDefinition.setOrderBy(fields);
        }

        return queryDefinition;
    }


    @Operation(context = QueryDefinition.class)
    public QueryDefinition params(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        final Object value = ctx.getASTFunction().jjtGetChild(0).jjtAccept(ctx.getEnv(), null);

        if (!(value instanceof Map))
        {
            throw new QueryTransformationException("params() parameter is not a map: "+ value);
        }

        queryDefinition.setParameters(
            (Map<String, Object>) value
        );

        return queryDefinition;
    }
    @Operation(context = QueryDefinition.class)
    public QueryDefinition filter(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        ASTFunction node = ctx.getASTFunction();

        if (node.jjtGetNumChildren() != 1)
        {
            throw new QueryTransformationException("Filter operation takes exactly one argument: " + node);
        }

        SimpleNode expr = (SimpleNode) node.jjtGetChild(0);

        QueryCondition condition = transformFilter(env, queryDefinition.getQueryDomainType(), expr);
        if (condition != null)
        {
            QueryCondition existing = queryDefinition.getFilter();
            if (existing != null)
            {
                Set<DataField> refs = new HashSet<>();
                refs.addAll(existing.getFieldReferences());
                refs.addAll(condition.getFieldReferences());

                queryDefinition.setFilter(new QueryCondition(
                        existing.getCondition()
                            .and(condition.getCondition()),
                        refs
                    )
                );
            }
            else
            {
                queryDefinition.setFilter(condition);
            }
        }
        return queryDefinition;
    }


    private QueryCondition transformFilter(QueryTransformerEnvironment env, QueryDomainType queryDomainType, SimpleNode n)
    {
        try
        {
            if (expressionService == null)
            {
                throw new IllegalStateException("ExpressionService not set");
            }

            final QueryFilterEnvironment filterEnv = new QueryFilterEnvironment(env, queryDomainType.getLeftMost());
            final Condition condition = (Condition) expressionService.evaluate(n, filterEnv);

            return new QueryCondition(condition, filterEnv.getFieldReferences());
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException("Error transforming filter expression '" + ExpressionRenderer.render(n) + "' for " + env.getComponentModel(), e);
        }
    }


    private String evaluateOrderByArg(Node n)
    {
        if (n instanceof ASTIdentifier)
        {
            return ((ASTIdentifier) n).getName();
        }
        else if (n instanceof ASTPropertyChain)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n.jjtGetNumChildren(); i++)
            {
                Node kid = ((ASTPropertyChain) n).getChainChild(i);

                if (i > 0)
                {
                    sb.append('.');
                }

                if (kid instanceof ASTIdentifier)
                {
                    sb.append(((ASTIdentifier) kid).getName());
                }
                else
                {
                    throw new QueryTransformationException("Invalid node in orderBy arguments: " + kid);
                }
            }
            return sb.toString();
        }
        else
        {
            throw new QueryTransformationException("Invalid node in orderBy arguments: " + n);
        }
    }

    @Operation
    public QueryDefinition query(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        Object result = ExpressionUtil.visitOneChildOf(ctx.getEnv(), ctx.getASTFunction(), ASTFunction.class, ASTPropertyChain.class, ASTIdentifier.class);

//        if (result == null)
//        {
//            return new QueryDefinition(null);
//        }

        if (!(result instanceof QueryDomainType))
        {
            throw new QueryTransformationException("Argument to query() is no domain type definition: "
                + result);

        }

        return new QueryDefinition((QueryDomainType) result);
    }

    @Operation
    public Conditional when(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        final ASTFunction astFunction = ctx.getASTFunction();
        if (astFunction.jjtGetNumChildren() != 1)
        {
            throw new QueryTransformationException("when(cond) takes exactly 1  condition argument");
        }

        final Object conditionResult = astFunction.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);

        if (!(conditionResult instanceof Boolean))
        {
            throw new QueryTransformationException("when(cond): condition did not evaluate to boolean value");
        }

        return new Conditional((Boolean) conditionResult);
    }

    @Operation(context = Conditional.class)
    public Object then(ExpressionContext<QueryTransformerEnvironment> ctx, Conditional conditional)
    {
        final ASTFunction thenFn = ctx.getASTFunction();
        if (conditional.isTrue())
        {
            return thenFn.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);
        }
        else
        {
            if (thenFn.jjtGetParent().jjtGetNumChildren() == 3)
            {
                return conditional;
            }
            else
            {
                return null;
            }
        }
    }

    @Operation(name = "else", context = Conditional.class)
    public Object elseOperation(ExpressionContext<QueryTransformerEnvironment> ctx, Conditional conditional)
    {
        if (conditional.isTrue())
        {
            throw new IllegalStateException("Else should never be invoked with a true condition");
        }

        final ASTFunction elseFn = ctx.getASTFunction();
        return elseFn.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);
    }

    @Operation
    public QueryDomainType domainType(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        final ASTFunction astFunction = ctx.getASTFunction();
        if (astFunction.jjtGetNumChildren() != 1)
        {
            throw new QueryTransformationException("type(name) takes exactly one domain type name argument");
        }

        final String name = ExpressionUtil.visit(astFunction.jjtGetChild(0), ctx.getEnv(), String.class);

        final DomainType domainType = ctx.getEnv().getRuntimeContext().getApplicationModel().getDomainType(name);
        return new QueryDomainType(domainType);
    }

    @Operation
    public PropertyModel formFieldType(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        final ComponentModel componentModel = ctx.getEnv().getComponentModel();
        final ContextVarInfo info = findVarName(ctx, componentModel, new ArrayList<>());
        final String scopeVarName = info.getVarName();
        String fieldName = getFieldReference(componentModel);

        final ApplicationModel applicationModel = ctx.getEnv().getRuntimeContext().getApplicationModel();
        final View viewModel = ctx.getEnv().getViewModel();
        final ScopeDeclarations declarations = applicationModel.lookup(
            viewModel
        );

        PropertyModel propertyModel;
        final ScopeDeclaration scopeDeclaration = declarations.get(scopeVarName);

        if (scopeDeclaration == null)
        {
            throw new QueryTransformationException(
                "Scoped value '" + scopeVarName + "' referenced by " + componentModel + " not found");
        }

        if (scopeVarName.equals(ProcessContext.CURRENT))
        {
            propertyModel = DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, viewModel.getDomainType())
                .build();
        }
        else
        {
            propertyModel = scopeDeclaration.getModel();
        }

        if (info.isIterative())
        {
            propertyModel = ExpressionUtil.getCollectionType(applicationModel, propertyModel.getTypeParam());
        }

        final String type = propertyModel.getType();
        switch (type)
        {
            case PropertyType.MAP:                                return ExpressionUtil.getCollectionType(
                    applicationModel,
                    propertyModel.getTypeParam()
                );
            case PropertyType.DOMAIN_TYPE:
                final DomainType domainType = applicationModel.getDomainType(propertyModel.getTypeParam());
                return domainType.getProperty(fieldName);
            default:
                throw new QueryTransformationException("Scope value '" + scopeVarName + "': Cannot determine property '" + fieldName + "' for " + ExpressionUtil.describe(propertyModel));
        }
    }

    @Operation
    public Object exec(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        final ASTFunction astFunction = ctx.getASTFunction();

        if (astFunction.jjtGetNumChildren() != 1)
        {
            throw new InvalidExpressionException("exec(queryDefinition) takes exactly 1 argument");
        }

        final Object o = astFunction.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);

        if (o == null)
        {
            return null;

        }
        if (!(o instanceof QueryDefinition))
        {
            throw new InvalidExpressionException("exec(queryDefinition): Argument did not evaluate to query definition, but " + o);
        }

        QueryDefinition queryDefinition = (QueryDefinition) o;
        return ctx.getEnv().executeQuery(queryDefinition);
    }

    @Operation
    public Object actionQuery(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        final ASTFunction astFunction = ctx.getASTFunction();

        final Object o = astFunction.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);

        if (!(o instanceof String) || !ctx.getEnv().hasAction((String) o))
        {
            throw new InvalidExpressionException("action(): " + o + " is not a valid action name");
        }

        final ActionResult result = ctx.getEnv().execute((String) o, new QueryTransformerActionParameters(
            ctx, astFunction
        ));

        if (!result.isResolved())
        {
            throw new IllegalStateException("Action was rejected", (Throwable) result.get());
        }

        return result.get();

    }

    private String getFieldReference(ComponentModel componentModel)
    {
        final ASTExpression cursorExpr = componentModel.getAttribute("value").getAstExpression();

        final Node chain = cursorExpr.jjtGetChild(0);
        if (!(chain instanceof ASTPropertyChain) || !(chain.jjtGetChild(1) instanceof ASTPropertyChainDot))
        {
            throw new QueryTransformationException(
                "Invalid cursor expression : " + ExpressionUtil.renderExpressionOf(cursorExpr)
            );
        }
        return ((ASTIdentifier) chain.jjtGetChild(1).jjtGetChild(0)).getName();
    }


    private ContextVarInfo findVarName(
        ExpressionContext<QueryTransformerEnvironment> ctx,
        ComponentModel componentModel,
        List<ComponentModel> visited
    )
    {
        visited.add(componentModel);

        final ComponentDescriptor descriptor = componentModel.getComponentRegistration().getDescriptor();
        if (!descriptor.hasClass(ComponentClasses.FIELD) && !descriptor.hasClass(ComponentClasses.FORM_CONTAINER))
        {
            throw new QueryTransformationException(componentModel + " is not a form field component");
        }

        final ASTExpression expression = getComponentExpression(componentModel, VALUE_ATTRIBUTE);
        // does the component have an expression referencing "context"?
        if (expression != null && !ExpressionUtil.hasContextReference(expression))
        {
            // no -> must reference other context variable
            return new ContextVarInfo(
                extractContextVarReference(ctx, expression),
                false
            );
        }


        final ComponentModel parent = ComponentUtil.findParent(
            componentModel,
            new ComponentUtil.HasClassPredicate(ComponentClasses.FORM_CONTAINER)
        );

        if (parent == null)
        {
            throw new InconsistentModelException("No data source found in component parent chain " + Util.join(visited, " -> "));
        }

        // predicate ensured we have a registration and descriptor
        final ComponentDescriptor parentDescriptor = parent.getComponentRegistration().getDescriptor();

        final ExpressionValue dataAttr = parent.getAttribute(DATA_ATTRIBUTE);
        if (dataAttr == null || dataAttr.getAstExpression() == null)
        {
            return findVarName(ctx, parent, visited);
        }

        final ASTExpression astExpression = dataAttr.getAstExpression();
        final Node node = astExpression.jjtGetChild(0);
        if (!(node instanceof ASTIdentifier))
        {
            throw new QueryTransformationException(
                "Invalid data cursor expression in " + componentModel + ": " + ExpressionUtil
                    .renderExpressionOf(astExpression));
        }

        return new ContextVarInfo(((ASTIdentifier) node).getName(), parentDescriptor.hasClass(ComponentClasses.ITERATIVE_CONTEXT));
    }


    private String extractContextVarReference(
        ExpressionContext<QueryTransformerEnvironment> ctx,
        ASTExpression expression
    )
    {
        final Node first;

        if (expression.jjtGetChild(0) instanceof ASTPropertyChain)
        {
            first = expression.jjtGetChild(0).jjtGetChild(0);
        }
        else
        {
            first = expression.jjtGetChild(0);
        }

        final RuntimeContext runtimeContext = ctx.getEnv().getRuntimeContext();
        final Definitions definitions = runtimeContext.getApplicationModel().lookup(
            ctx.getEnv().getViewModel().getScopeLocation()).getLocalDefinitions();

        if (first instanceof ASTIdentifier)
        {
            final String varName = ((ASTIdentifier) first).getName();
            final Definition definition = definitions.getDefinition(varName);
            if (definition != null && definition.getDefinitionType() == DefinitionType.CONTEXT)
            {
                return varName;
            }
        }

        throw new InvalidExpressionException("Invalid form context reference: " + ExpressionRenderer.render(expression));
    }


    private ASTExpression getComponentExpression(ComponentModel componentModel, String attrName)
    {
        final ExpressionValue attribute = componentModel.getAttribute(attrName);
        if (attribute != null)
        {
            return attribute.getAstExpression();
        }

        final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
        PropDeclaration propDecl;
        if (
            registration != null &&
            (propDecl = registration.getDescriptor().getPropTypes().get(attrName)) != null &&
            propDecl.getDefaultValue() != null
        )
        {
            return propDecl.getDefaultValue().getAstExpression();
        }

        return null;
    }


    private static class ComponentWithDataCursorPredicate
            implements Predicate<ComponentModel>
    {
        @Override
        public boolean test(ComponentModel c)
        {
            return c.getComponentRegistration() != null &&
                DATA_CURSOR.equals(c.getComponentRegistration().getDescriptor().getProvidesContext());
        }
    }
}

package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTArray;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.LiteralValueNode;
import de.quinscape.exceed.expression.LogicalOperatorNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.Operator;
import de.quinscape.exceed.expression.OperatorNode;
import de.quinscape.exceed.expression.PropertyChainLink;
import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.domain.property.DecimalConverter;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.FunctionDefinition;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TypeAnalyzer
{
    private final static Logger log = LoggerFactory.getLogger(TypeAnalyzer.class);

    public final static int DECIMAL_PRIORITY = 1;
    public final static int STRING_PRIORITY = 2;

    private final static Map<String,Integer> priorities = new HashMap<>();

    private final static String FOREIGN_VALUE = "ForeignValue";

    static
    {
        priorities.put(PropertyType.INTEGER, 0);
        priorities.put(PropertyType.DECIMAL, DECIMAL_PRIORITY);
        priorities.put(PropertyType.PLAIN_TEXT, STRING_PRIORITY);
        priorities.put(PropertyType.RICH_TEXT, STRING_PRIORITY + 1);
        priorities.put(PropertyType.UUID, STRING_PRIORITY + 2);
        priorities.put(PropertyType.ENUM, STRING_PRIORITY + 2);
        priorities.put(PropertyType.STATE, STRING_PRIORITY + 2);

    }

    public PropertyModel analyze(TypeAnalyzerContext context, Node node)
    {
        log.debug("analyze {}, {}", context, node);

        if (node instanceof ASTFunction)
        {
            final Definition definition = context.getContextDefinitions().getDefinition(((ASTFunction) node).getName());
            if (definition instanceof FunctionDefinition)
            {
                if (analyzeFunctionParameter(context, (FunctionDefinition) definition, node))
                {
                    final DomainProperty foreign = DomainProperty.builder().withType(FOREIGN_VALUE).build();
                    node.annotation().setPropertyType(foreign);
                    return foreign;
                }
            }
            else
            {
                analyzeChildren(context, node);
            }
        }
        else if (node instanceof ASTPropertyChain)
        {
            analyzeChainChildren(context, node);
        }
        else
        {
            analyzeChildren(context, node);
        }

        final PropertyModel resolvedType = resolvePropertyType(context, node);
        node.annotation().setPropertyType(resolvedType);

        return resolvedType;
    }


    private void analyzeChainChildren(TypeAnalyzerContext context, Node node)
    {
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            final Node kid = node.jjtGetChild(i);
            PropertyModel result = analyze(context, kid);
            if (result != null && result.getType().equals(FOREIGN_VALUE))
            {
                break;
            }
        }
    }


    private boolean analyzeFunctionParameter(TypeAnalyzerContext context, FunctionDefinition definition, Node node)
    {
        boolean hasExpressionChildren = false;

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            final DomainProperty parameterModel = getNthParameterModel(definition, node, i);

            final Node kid = node.jjtGetChild(i);
            if (parameterModel.equals(ExpressionUtil.EXPRESSION_TYPE))
            {
                hasExpressionChildren = true;
            }
            else
            {
                analyze(context, kid);
            }
        }

        return hasExpressionChildren;

    }


    private DomainProperty getNthParameterModel(
        FunctionDefinition definition, Node node, int i
    )
    {
        final List<DomainProperty> parameterModels = definition.getParameterModels();
        if (i < parameterModels.size())
        {
            return parameterModels.get(i);
        }
        else
        {
            if (definition.isVarArgs())
            {
                return parameterModels.get(parameterModels.size() - 1);
            }
            throw new InvalidExpressionException(definition + " has only " + i + " parameters : " + ExpressionUtil.renderExpressionOf(node));
        }
    }


    private void analyzeChildren(TypeAnalyzerContext context, Node node)
    {
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            final Node kid = node.jjtGetChild(i);
            analyze(context, kid);
        }
    }


    public PropertyModel resolveReference(TypeAnalyzerContext context, ResolvableNode node)
    {
        final boolean function = node instanceof ASTFunction;
        final String name = node.getName();

        final Definition definition = context.getContextDefinitions().getDefinition(name);
        if (definition != null)
        {
            if (definition.isFunction() != function)
            {
                throw new InvalidReferenceException(
                    "Definition '" + name + "' is " + ( function ? "not a function" : "not on identifier" )
                );
            }
            return definition.getType(context, node, context.getContextModel());
        }

        throw new InvalidReferenceException("Cannot resolve " + node + ": " + ExpressionUtil.renderExpressionOf(node));
    }


    private PropertyModel resolvePropertyType(TypeAnalyzerContext context, Node node)
    {
        // is this element part of a property chain (excluding the first part)?
        final boolean isLinked = ExpressionUtil.isLinkedNode(node);

        final PropertyModel existing = node.annotation().getPropertyType();
        if (existing != null)
        {
            return existing;
        }

        if (node instanceof PropertyChainLink)
        {
            return node.jjtGetChild(0).annotation().getPropertyType();
        }
        else if (node instanceof ASTFunction && !isLinked)
        {
            return resolveReference(context,(ASTFunction) node);
        }
        else if (node instanceof ASTPropertyChain)
        {
            final Node first = node.jjtGetChild(0);
            PropertyModel propertyType = first.annotation().getPropertyType();

//            final Node last = node.jjtGetChild(node.jjtGetNumChildren() - 1).jjtGetChild(0);
//            final PropertyModel typeAtChainEnd = last.annotation().getPropertyType();
//            if (typeAtChainEnd != null)
//            {
//                return typeAtChainEnd;
//            }

            if (propertyType != null)
            {
                if (propertyType.getType().equals(FOREIGN_VALUE))
                {
                    return null;
                }

                return context.walk((ASTPropertyChain) node, propertyType, 1);
            }
        }
        else if (node instanceof LiteralValueNode)
        {
            return ((LiteralValueNode) node).getLiteralType();
        }
        else if (node instanceof ASTMap)
        {
            return DomainProperty.builder()
                .withType(PropertyType.MAP, getCollectionType(node, PropertyType.MAP))
                .build();
        }
        else if (node instanceof ASTArray)
        {
            return DomainProperty.builder()
                .withType(PropertyType.LIST, getCollectionType(node, PropertyType.LIST))
                .build();
        }
        else if (node instanceof ASTEquality)
        {
            checkEquality((ASTEquality) node);
            return ExpressionUtil.BOOLEAN_TYPE;
        }
        else if (node instanceof ASTRelational)
        {
            checkRelational((ASTRelational) node);
            return ExpressionUtil.BOOLEAN_TYPE;
        }
        else if (node instanceof LogicalOperatorNode)
        {
            return ExpressionUtil.BOOLEAN_TYPE;
        }
        else if (node instanceof OperatorNode)
        {
            final Operator op = ((OperatorNode) node).getOperator();

            return widen(context, (OperatorNode) node, op == Operator.ADD);
        }
        else if (node instanceof ASTExpression)
        {
            return node.jjtGetChild(0).annotation().getPropertyType();
        }
        else if (node instanceof ASTIdentifier && !isLinked)
        {
            return resolveReference(context,( ASTIdentifier)node);
        }
        return null;
    }


    private String getCollectionType(Node node, String type)
    {
        if (node.jjtGetNumChildren() == 0)
        {
            return PropertyType.OBJECT;
        }

        final int entryCount = node.jjtGetNumChildren();

        PropertyModel collectionType = getNthValueType(node, 0, type).annotation().getPropertyType();

        if (collectionType == null)
        {
            return PropertyType.OBJECT;
        }

        for (int i=0; i < entryCount ; i++)
        {
            final PropertyModel current = getNthValueType(node, i, type).annotation().getPropertyType();

            if (current == null)
            {
                return PropertyType.OBJECT;
            }
            else
            {
                final String currentType = current.getType();
                final boolean isDomainType = currentType.equals(PropertyType.DOMAIN_TYPE);
                boolean isNamed = isNamedType(currentType);

                if (currentType.equals(collectionType.getType()))
                {
                    if (isNamed && !Objects.equals(collectionType.getTypeParam(), current.getTypeParam()))
                    {
                        if (isDomainType && collectionType.getType().equals(PropertyType.DOMAIN_TYPE))
                        {
                            collectionType = ExpressionUtil.GENERIC_DOMAIN_TYPE;
                        }
                        else
                        {
                            return PropertyType.OBJECT;
                        }
                    }
                }
                else
                {
                    return PropertyType.OBJECT;
                }
            }
        }

        boolean isNamed = isNamedType(collectionType.getType());
        if (isNamed)
        {
            final String typeParam = collectionType.getTypeParam();
            if (typeParam == null)
            {
                return collectionType.getType();
            }
            return typeParam;
        }
        else
        {
            return collectionType.getType();
        }
    }


    private boolean isNamedType(String currentType)
    {
        return currentType.equals(PropertyType.DOMAIN_TYPE) || currentType.equals(PropertyType.ENUM) || currentType.equals(PropertyType.STATE);
    }


    private Node getNthValueType(Node node, int index, String type)
    {
        if (type.equals(PropertyType.MAP))
        {
            return node.jjtGetChild(index).jjtGetChild(1);
        }
        else
        {
            return node.jjtGetChild(index);
        }
    }


    private void checkEquality(ASTEquality node)
    {
        checkEnumOrStateEquality(node, PropertyType.ENUM);
        checkEnumOrStateEquality(node, PropertyType.STATE);

    }


    private void checkEnumOrStateEquality(ASTEquality node, String type)
    {
        final PropertyModel typeA = node.jjtGetChild(0).annotation().getPropertyType();
        final PropertyModel typeB = node.jjtGetChild(1).annotation().getPropertyType();

        boolean aIsEnum = typeA != null && typeA.getType().equals(type);
        boolean bIsEnum = typeB != null && typeB.getType().equals(type);

        if (aIsEnum == bIsEnum)
        {
            if (aIsEnum)
            {
                if (!typeA.getTypeParam().equals(typeB.getTypeParam()))
                {
                    throw new InvalidTypeException(type + " type mismatch: " + ExpressionUtil.renderExpressionOf(node));
                }
            }
        }
        else
        {
            throw new InvalidTypeException("Invalid equality check between enum" + ExpressionUtil.describe(typeA) + " and " + ExpressionUtil.describe(typeB) + ": " + ExpressionUtil.renderExpressionOf(node));
        }
    }


    private void checkRelational(ASTRelational node)
    {
        checkRelational(node, node.jjtGetChild(0));
        checkRelational(node, node.jjtGetChild(1));

    }


    private void checkRelational(ASTRelational astRelational, Node node)
    {
        final PropertyModel type = node.annotation().getPropertyType();
        if ( type != null && (type.getType().equals(PropertyType.ENUM) || type.getType().equals(PropertyType.STATE)))
        {
            throw new InvalidTypeException(ExpressionUtil.describe(type) + " does not support operator: " + ExpressionUtil.renderExpressionOf(astRelational));
        }
    }


    public static PropertyModel widen(TypeAnalyzerContext context, OperatorNode node, boolean allowStrings)
    {
        int currentPriority = -1;
        int currentPlaces = -1;

        PropertyModel current = null;

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            final PropertyModel type = node.jjtGetChild(i).annotation().getPropertyType();
            if (type == null)
            {
                return null;
            }

            Integer priority = priorities.get(type.getType());

            if (priority == null)
            {
                throw new InvalidTypeException(ExpressionUtil.describe(type) + " does not support operator " + ExpressionUtil.renderExpressionOf(node));
            }

            if (priority > currentPriority)
            {
                if (!allowStrings && priority >= STRING_PRIORITY)
                {
                    throw new InvalidTypeException(ExpressionUtil.describe(type) + " is not allowed for operator " + ExpressionUtil.renderExpressionOf(node));
                }

                currentPriority = priority;
                current = type;
            }

            if (currentPriority == DECIMAL_PRIORITY)
            {
                Integer decimalPlaces = DecimalConverter.getDecimalPlaces(context.getApplicationModel().getConfigModel().getDecimalConfig().getDefaultDecimalPlaces(), type.getConfig());
                if (decimalPlaces > currentPlaces)
                {
                    currentPlaces = decimalPlaces;
                    current = type;
                }
            }
        }
        return current;
    }
}

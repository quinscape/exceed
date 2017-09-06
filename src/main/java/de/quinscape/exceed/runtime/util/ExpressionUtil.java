package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.LiteralValueNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.PropertyChainLink;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.config.ExpressionConfiguration;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainTypesRegistry;
import de.quinscape.exceed.runtime.domain.GeneratedDomainObject;
import de.quinscape.exceed.runtime.domain.property.DomainObjectConverter;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironmentException;
import de.quinscape.exceed.runtime.expression.transform.ComponentExpressionTransformer;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class ExpressionUtil
{
    public static final DomainProperty PLAINTEXT_TYPE = DomainProperty.builder()
        .withType(PropertyType.PLAIN_TEXT)
        .build();

    public static final DomainProperty INTEGER_TYPE = DomainProperty.builder().withType(PropertyType.INTEGER).build();

    public static final DomainProperty BOOLEAN_TYPE = DomainProperty.builder().withType(PropertyType.BOOLEAN).build();

    public static final DomainProperty DATE_TYPE = DomainProperty.builder().withType(PropertyType.DATE).build();

    public static final DomainProperty TIMESTAMP_TYPE = DomainProperty.builder().withType(PropertyType.TIMESTAMP)
        .build();

    public static final DomainProperty DECIMAL_TYPE = DomainProperty.builder().withType(PropertyType.DECIMAL).build();

    public static final DomainProperty GENERIC_DOMAIN_TYPE = DomainProperty.builder()
        .withType(PropertyType.DOMAIN_TYPE)
        .build();

    public static final DomainProperty DATA_GRAPH_TYPE = DomainProperty.builder()
        .withType(PropertyType.DATA_GRAPH)
        .build();

    public static final DomainProperty OBJECT_TYPE = DomainProperty.builder().withType(PropertyType.OBJECT).build();

    public static final DomainProperty STATE_TYPE = DomainProperty.builder().withType(PropertyType.STATE).build();

    /** pseudo property type used for foreign expression embedding */
    public static final DomainProperty EXPRESSION_TYPE = DomainProperty.builder().withType("Expression").build();


    @SafeVarargs
    public static Object visitOneChildOf(ExpressionParserVisitor visitor, ASTFunction n, Class<? extends Node>...
        classes)
    {
        if (n.jjtGetNumChildren() != 1)
        {
            throw new ExpressionEnvironmentException("Expected exactly 1 argument of " + Arrays.toString(classes) +
                " for function " + n.getName() + "'");
        }

        Node firstArg = n.jjtGetChild(0);
        for (Class<? extends Node> cls : classes)
        {
            if (cls.isInstance(firstArg))
            {
                return firstArg.jjtAccept(visitor, null);
            }
        }

        throw new ExpressionEnvironmentException(
            "Unexpected argument for function " + n.getName() + "': " + firstArg
        );
    }

    @SafeVarargs
    public static Object visitNthChildOf(ExpressionParserVisitor visitor, ASTFunction n, int index, Class<? extends Node>...
        classes)
    {
        if (n.jjtGetNumChildren() <= index)
        {
            throw new ExpressionEnvironmentException("Expected at least " + index + " arguments of " + Arrays.toString(classes) +
                " for function " + n.getName() + "'");
        }

        Node nthArg = n.jjtGetChild(index);
        for (Class<? extends Node> cls : classes)
        {
            if (cls.isInstance(nthArg))
            {
                return nthArg.jjtAccept(visitor, null);
            }
        }

        throw new ExpressionEnvironmentException(
            "Unexpected argument for function " + n.getName() + "': " + nthArg
        );
    }


    @SafeVarargs
    public static SimpleNode expectChildOf(ASTFunction n, Class<? extends Node>... classes)
    {
        if (n.jjtGetNumChildren() != 1)
        {
            throw new ExpressionEnvironmentException("Expected exactly 1 argument of " + Arrays.toString(classes) +
                " for function " + n.getName() + "'");
        }

        SimpleNode firstArg = (SimpleNode) n.jjtGetChild(0);
        for (Class<? extends Node> cls : classes)
        {
            if (cls.isInstance(firstArg))
            {
                return firstArg;
            }
        }

        throw new ExpressionEnvironmentException("Unexpected argument for function " + n.getName() + "': " +
            firstArg);
    }


    public static <T> T visit(Node node, ExpressionParserVisitor visitor, Class<T> cls)
    {
        Object value = node.jjtAccept(visitor, null);

        if (value == null || cls.isInstance(value))
        {
            return (T) value;
        }

        throw new IllegalArgumentException(value + " is no " + cls.getName());
    }


    public static boolean validAssignmentTarget(String name)
    {
        return name.equals("property") || name.equals("object") || name.equals("list");
    }


    /**
     * Returns the index the given node has among its siblings within the parent children.
     *
     * @param node non-root node
     * @return index
     */
    public static int findIndexInParent(Node node)
    {
        Node parent = node.jjtGetParent();

        if (parent == null)
        {
            throw new IllegalStateException("Node " + node + " has no parent");
        }

        for (int i = 0; i < parent.jjtGetNumChildren(); i++)
        {
            if (parent.jjtGetChild(i) == node)
            {
                return i;
            }
        }
        throw new IllegalStateException("Node not in children of parent");
    }

    public static void replaceNode(Node n, Node replacement)
    {
        final int indexInParent = findIndexInParent(n);

        final Node parent = n.jjtGetParent();
        parent.jjtAddChild(replacement, indexInParent);
        replacement.jjtSetParent(parent);
        n.jjtSetParent(null);
    }


    public static Node getFromMap(ASTMap astMap, String name)
    {
        for (int i = 0; i < astMap.jjtGetNumChildren(); i++)
        {
            ASTMapEntry e = (ASTMapEntry) astMap.jjtGetChild(i);
            Node nameNode = e.jjtGetChild(0);

            String key;

            if (nameNode instanceof ASTIdentifier)
            {
                key = ((ASTIdentifier) nameNode).getName();
            }
            else
            {
                key = ((ASTString) nameNode).getValue();
            }

            if (name.equals(key))
            {
                return e.jjtGetChild(1);
            }
        }
        return null;
    }


    /**
     * Evaluates a simple value expression that interprets all identifier constructs as literal strings and returns
     * the value of the first value token.
     */
    public static Object evaluateSimple(String expr)
    {
        final ASTExpression astExpression;
        try
        {
            astExpression = ExpressionParser.parse(expr);
        }
        catch (ParseException e)
        {
            // if we fail to parse the expression, we return it as source string
            return expr;
        }

        final Node first = astExpression.jjtGetChild(0);

        if (first instanceof ASTPropertyChain || first instanceof ASTIdentifier)
        {
            return ExpressionRenderer.render(astExpression);
        }
        else if (first instanceof LiteralValueNode)
        {
            return ((LiteralValueNode) first).getLiteralValue();
        }

        throw new IllegalArgumentException("Expression too complex for simple evaluation:" + expr);
    }


    public static DomainProperty getPropertyModelFor(Class<?> paramType)
    {
        final DomainProperty propertyModel = findPropertyModelFor(paramType);

        if (propertyModel == null)
        {
            throw new IllegalStateException("Cannot conclude property type from java type: " + paramType);
        }

        return propertyModel;
    }


    /**
     * Returns a property type for the given java type if possible.
     *
     * @param paramType     java type
     *
     * @return  property type of <code>null</code>
     */
    public static DomainProperty findPropertyModelFor(Class<?> paramType)
    {
        if (paramType.equals(String.class))
        {
            return PLAINTEXT_TYPE;
        }
        else if (paramType.equals(Integer.class) || paramType.equals(Integer.TYPE))
        {
            return INTEGER_TYPE;
        }
        else if (paramType.equals(Boolean.class) || paramType.equals(Boolean.TYPE))
        {
            return BOOLEAN_TYPE;
        }
        else if (paramType.equals(Date.class))
        {
            return DATE_TYPE;
        }
        else if (paramType.equals(Timestamp.class))
        {
            return TIMESTAMP_TYPE;
        }
        else if (paramType.equals(BigDecimal.class))
        {
            return DECIMAL_TYPE;
        }
        else if (paramType.equals(DataGraph.class))
        {
            return DATA_GRAPH_TYPE;
        }
        else if (paramType.equals(ASTExpression.class))
        {
            return EXPRESSION_TYPE;
        }
        else if (paramType.equals(Object.class))
        {
            return OBJECT_TYPE;
        }
        else if (paramType.isArray())
        {
            final DomainProperty elemType = findPropertyModelFor(paramType.getComponentType());
            final String typeParam;

            if (elemType == null)
            {
                typeParam = null;
            }
            else
            {
                if (elemType.getType().equals(PropertyType.DOMAIN_TYPE))
                {
                    typeParam = elemType.getTypeParam();
                }
                else
                {
                    typeParam = elemType.getType();
                }
            }


            return DomainProperty.builder()
                .withType(PropertyType.LIST, typeParam)
                .build();
        }
        else if (GeneratedDomainObject.class.isAssignableFrom(paramType))
        {
            return DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, paramType.getSimpleName())
                .withConfig(DomainObjectConverter.IMPLEMENTATION_CONFIG, paramType.getName())
                .build();
        }
        else if (DomainObject.class.isAssignableFrom(paramType))
        {
            return GENERIC_DOMAIN_TYPE;
        }
        else if (Map.class.isAssignableFrom(paramType))
        {
            return DomainProperty.builder()
                .withType(PropertyType.MAP, PropertyType.OBJECT)
                .build();
        }
        else if (List.class.isAssignableFrom(paramType))
        {
            return DomainProperty.builder()
                .withType(PropertyType.LIST, PropertyType.OBJECT)
                .build();
        }
        return null;
    }


    /**
     * Returns a rendered expression string based on one of its nodes.
     *
     * @param node  node
     * @return  expression string
     */
    public static String renderExpressionOf(Node node)
    {
        Node parent;
        Node current = node;
        while ((parent = current.jjtGetParent()) != null)
        {
            current = parent;
        }

        return node + " in { " + ExpressionRenderer.render(current) + " }";
    }

    public static boolean isFirstAmongSiblings(Node node)
    {
        return node == node.jjtGetParent().jjtGetChild(0);
    }

    public static String getNameOrValue(Node node)
    {
        if (node instanceof ASTIdentifier)
        {
            return ((ASTIdentifier) node).getName();
        }
        else if (node instanceof ASTString)
        {
            return ((ASTString) node).getValue();
        }
        throw new IllegalStateException("Node must be identifier or string: "+ node);
    }

    public static Object getNameOrLiteralValue(Node node)
    {
        if (node instanceof ASTIdentifier)
        {
            return ((ASTIdentifier) node).getName();
        }
        else if (node instanceof LiteralValueNode)
        {
            return ((LiteralValueNode) node).getLiteralValue();
        }
        throw new IllegalStateException("Node must be identifier literal node: "+ node);
    }

    public static ExpressionType getExpressionType(PropDeclaration propDecl)
    {
        if (propDecl == null)
        {
            return ExpressionType.VALUE;
        }

        final PropType type = propDecl.getType();
        switch(type)
        {
            case PLAINTEXT:
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
            case CLASSES:
            case TRANSITION:
            case MAP:
            case FIELD_REFERENCE:
            case DOMAIN_TYPE_REFERENCE:
            case STATE_MACHINE_REFERENCE:
            case GLYPH_ICON:
                return ExpressionType.VALUE;

            case QUERY_EXPRESSION:
                return ExpressionType.QUERY;
            case CURSOR_EXPRESSION:
                return ExpressionType.CURSOR;
            case CONTEXT_EXPRESSION:
                return ExpressionType.CONTEXT;
            case FILTER_EXPRESSION:
                return ExpressionType.FILTER;
            case VALUE_EXPRESSION:
                return ExpressionType.VALUE;
            case ACTION_EXPRESSION:
                return ExpressionType.ACTION;
            default:
                throw new IllegalStateException("Unhandled type: " + type);
        }
    }

    public static String dump(Node node)
    {
        StringBuilder sb = new StringBuilder();
        dumpRec(sb, node, 0);
        return "\n" + sb.toString();
    }

    private static void indent(StringBuilder sb, int level)
    {
        for (int i = 0; i < level; i++)
        {
            sb.append("  ");
        }
    }


    private static void dumpRec(StringBuilder sb, Node node, int level)
    {
        indent(sb, level);
        sb.append(node).append("\n");

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            dumpRec(sb, node.jjtGetChild(i), level + 1);
        }
    }


    public static PropertyModel getPropertyType(String elemTypeName)
    {
        switch (elemTypeName)
        {
            case PropertyType.PLAIN_TEXT:
                return PLAINTEXT_TYPE;
            case PropertyType.INTEGER:
                return INTEGER_TYPE;
            case PropertyType.BOOLEAN:
                return BOOLEAN_TYPE;
            case PropertyType.DATE:
                return DATE_TYPE;
            case PropertyType.TIMESTAMP:
                return TIMESTAMP_TYPE;
            case PropertyType.DECIMAL:
                return DECIMAL_TYPE;
            case PropertyType.DOMAIN_TYPE:
                return GENERIC_DOMAIN_TYPE;
            case PropertyType.OBJECT:
                return OBJECT_TYPE;
            default:
                return DomainProperty.builder()
                    .withType(elemTypeName)
                    .build();
        }
    }


    /**
     * Consults the given registry to decide whether the given former collection type param refers to
     * a domain type, an enum type or a property type.
     *
     * @param registry
     * @param elemTypeName
     * @return
     */
    public static PropertyModel getCollectionType(DomainTypesRegistry registry, String elemTypeName)
    {
        if (registry.getDomainTypes().containsKey(elemTypeName))
        {
            return DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, elemTypeName)
                .build();
        }
        else if (registry.getEnums().containsKey(elemTypeName))
        {
            return DomainProperty.builder()
                .withType(PropertyType.ENUM, elemTypeName)
                .build();
        }
        else if (registry.getStateMachines().containsKey(elemTypeName))
        {
            return DomainProperty.builder()
                .withType(PropertyType.STATE, elemTypeName)
                .build();
        }
        else
        {
            if (!registry.getPropertyTypes().containsKey(elemTypeName))
            {
                throw new IllegalArgumentException("Cannot resolve property type for typeParam '" + elemTypeName + "'");
            }
            return ExpressionUtil.getPropertyType(elemTypeName);
        }
    }


    /**
     * Returns a more readable textual description for a property model.
     * <ul>
     *     <li>
     *         <code>"Map&lt;Type&gt;"</code> / <code>"List&lt;Type&gt;"</code>
     *     </li>
     *     <li>
     *         <code>"MyEnumType"</code> / <code>"MyDomainType"</code>
     *     </li>
     *     <li>
     *         <code>"Plaintext"</code>, <code>"Integer"</code> etc.
     *     </li>
     * </ul>
     *
     * @param propertyModel    property model
     *
     * @return textual description.
     */
    public static String describe(PropertyModel propertyModel)
    {
        final String type = propertyModel.getType();
        final String typeParam = propertyModel.getTypeParam();

        switch (type)
        {
            case PropertyType.MAP:
            case PropertyType.LIST:
                if (typeParam == null || typeParam.equals(PropertyType.OBJECT))
                {
                    return type;
                }
                else
                {
                    return type + "<" + typeParam + ">";
                }

            case PropertyType.DOMAIN_TYPE:
            case PropertyType.ENUM:
            case PropertyType.STATE:
                if (typeParam == null)
                {
                    return type;
                }
                else
                {
                    return typeParam;
                }


            default:
                return type;
        }
    }


    /**
     * Returns true if the given node is not a secondary part of a property chain or a map key, and not a true
     * reference to its context identifier / context function.
     *
     *
     * @param node  identifier
     *
     * @return <code>true</code> if the node is not a true identifier / function.
     */
    public static boolean isLinkedNode(Node node)
    {
        final Node parent = node.jjtGetParent();
        if (parent instanceof PropertyChainLink)
        {
            // node is non-first link in a property chain
            return true;
        }
        else if (parent instanceof ASTMapEntry && parent.jjtGetChild(0) == node)
        {
            // node is key in a map entry
            return true;
        }
        return false;
    }


    /**
     * Returns true if any of the parent nodes is the lazyDependency() function.
     *
     * @param node      node
     * @return <code>true</code> if the node is supposed to be lazilyEvaluated
     */
    public static boolean isLazyDependency(Node node)
    {
        while ((node = node.jjtGetParent()) != null)
        {
            if (node instanceof ASTFunction && ((ASTFunction) node).getName().equals(ExpressionConfiguration.LAZY_DEPENDENCY))
            {
                return true;
            }
        }
        return false;
    }


    public static Node findNode(Node node, Predicate<Node> predicate)
    {
        if (node == null)
        {
            return null;
        }

        if (predicate.test(node))
        {
            return node;
        }

        final int count = node.jjtGetNumChildren();
        for (int i = 0; i < count ; i++)
        {
            final Node result = findNode(node.jjtGetChild(i), predicate);
            if (result != null)
            {
                return node;
            }
        }

        return null;
    }


    public static boolean hasContextReference(ASTExpression expression)
    {
        return ExpressionUtil.findNode(expression, ExpressionUtil.HasContextReferencePredicate.INSTANCE) != null;
    }


    public static class HasContextReferencePredicate
        implements Predicate<Node>
    {
        public final static HasContextReferencePredicate INSTANCE = new HasContextReferencePredicate();

        private HasContextReferencePredicate()
        {

        }

        @Override
        public boolean test(Node node)
        {
            return
                node instanceof ASTIdentifier &&
                ((ASTIdentifier) node).getName().equals(ComponentExpressionTransformer.CONTEXT_IDENTIFIER) &&
                // context identifier can be part of chain, but not as secondary link
                // ( "context" and "context.foo", but not "xxx.context" )
                !(node.jjtGetParent() instanceof PropertyChainLink);
        }
    }
}



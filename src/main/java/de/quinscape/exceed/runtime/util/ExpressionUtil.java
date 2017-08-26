package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.LiteralValueNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.domain.GeneratedDomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.domain.property.DomainTypeConverter;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironmentException;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import de.quinscape.exceed.runtime.service.ActionExpressionRenderer;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public static final DomainProperty OBJECT_TYPE = DomainProperty.builder().withType(PropertyType.OBJECT).build();

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

        throw new ExpressionEnvironmentException("Unexpected argument for function " + n.getName() + "': " +
            firstArg);
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

        if (cls.isInstance(value))
        {
            return (T) value;
        }

        throw new IllegalArgumentException(value + " is no " + cls.getName());
    }


    /**
     * Makes sure that the given expression sequence either consists only of actions or of no action generators
     *
     * @param seq                      property chain
     * @param actionExpressionRenderer action expression renderer
     * @return
     */
    public static boolean isChainOfActions(ASTExpressionSequence seq, ActionExpressionRenderer actionExpressionRenderer)
    {

        // Expression sequences must have more than 1 kids ( see Expression.jjt, ExpressionSequence() producer)
        Boolean isChainOfActions = isActionExpression(actionExpressionRenderer, seq.jjtGetChild(0));
        int i;
        for (i = 1; i < seq.jjtGetNumChildren(); i++)
        {
            Node kid = seq.jjtGetChild(i);

            if (isChainOfActions != isActionExpression(actionExpressionRenderer, kid))
            {
                throw new InvalidClientExpressionException("Expression sequence must be only action operations or " +
                    "have no action operations: " + ExpressionRenderer.render(seq));
            }
        }

        return isChainOfActions;
    }


    private static boolean isActionExpression(ActionExpressionRenderer actionExpressionRenderer, Node node)
    {
        boolean isAction = false;
        if (node instanceof ASTFunction)
        {
            String opName = ((ASTFunction) node).getName();
            isAction = actionExpressionRenderer.hasOperation(opName);
        }
        else if (node instanceof ASTAssignment)
        {
            /** @see #handleAssignmentAction(Node)  */
            throw new IllegalStateException("Unhandled assignment operator.");
        }
        return isAction;
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
    public static int findSiblingIndex(Node node)
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
        else if (paramType.equals(GenericDomainObject.class))
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
        else if (GeneratedDomainObject.class.isAssignableFrom(paramType))
        {
            return DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, paramType.getSimpleName())
                .withConfig(DomainTypeConverter.IMPLEMENTATION_CONFIG, paramType.getName())
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
        while ((parent = node.jjtGetParent()) != null)
        {
            node = parent;
        }

        return "{ " + ExpressionRenderer.render(node) + " }";
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
            case DOMAIN_TYPE_REFERENCE:
                return ExpressionType.VALUE;

            case QUERY_EXPRESSION:
                return ExpressionType.QUERY;
            case CURSOR_EXPRESSION:
                return ExpressionType.CURSOR;
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

    public static List<Object> getPath(ASTPropertyChain node, PropertyModel start, int startAt)
    {
        List<Object> list = new ArrayList<>();
        for (int i = startAt; i < node.jjtGetNumChildren(); i++)
        {
            Object nextProp = null;
            Node kid = node.jjtGetChild(i).jjtGetChild(0);

            if (kid instanceof ASTIdentifier)
            {
                nextProp = ((ASTIdentifier) kid).getName();
            }
            else if (kid instanceof ASTString)
            {
                nextProp = ((ASTString) kid).getValue();
            }
            else if (kid instanceof ASTInteger)
            {
                nextProp = ((ASTInteger) kid).getValue();
            }

            if (nextProp == null)
            {
                throw new InvalidExpressionException("Invalid chain link while walking type '" + start.getType() + ": " + ExpressionUtil.renderExpressionOf(node));
            }
            list.add(nextProp);
        }

        return list;
    }

    public static  PropertyModel walk(ApplicationModel applicationModel, ASTPropertyChain node, PropertyModel start, int startAt)
    {
        final List<Object> path = getPath(node, start, startAt);

        return walk(applicationModel, start, path);

    }


    private static PropertyModel walk(ApplicationModel applicationModel, PropertyModel start, List<Object> path)
    {
        PropertyModel current = start;

        for (Object nextProp : path)
        {
            if ( current.getType().equals(PropertyType.MAP) || current.getType().equals(PropertyType.LIST))
            {
                final String elemTypeName = (String) current.getTypeParam();

                if (applicationModel.getDomainTypes().containsKey(elemTypeName))
                {
                    current = DomainProperty.builder()
                        .withType(PropertyType.DOMAIN_TYPE, elemTypeName)
                        .build();
                }
                else
                {
                    current = getPropertyType(elemTypeName);
                }
            }
            else if ( current.getType().equals(PropertyType.DOMAIN_TYPE))
            {
                final DomainType domainType = applicationModel.getDomainType((String) current.getTypeParam());
                for (DomainProperty domainProperty : domainType.getProperties())
                {
                    if (domainProperty.getName().equals(nextProp))
                    {
                        current = domainProperty;
                        break;
                    }
                }
            }
            else
            {
                throw new InvalidExpressionException("Cannot walk type '" + current.getType() + ": " + path);
            }

        }
        return current;
    }


    private static PropertyModel getPropertyType(String elemTypeName)
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
    
}



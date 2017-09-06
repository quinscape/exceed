package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.ASTPropertyChainSquare;
import de.quinscape.exceed.expression.LiteralValueNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.expression.transform.ScopeExpressionTransformer;
import de.quinscape.exceed.runtime.expression.transform.StateMachineValueTransformer;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public final class TypeAnalyzerContext
{
    private static final String LENGTH = "length";

    private final ApplicationModel applicationModel;

    private final ExpressionType expressionType;

    private final Definitions contextDefinitions;

    private final ExpressionModelContext contextModel;

    private final TypeAnalyzer typeAnalyzer;


    public TypeAnalyzerContext(
        ApplicationModel applicationModel,
        ExpressionType expressionType,
        Definitions contextDefinitions,
        ExpressionModelContext contextModel,
        TypeAnalyzer typeAnalyzer)
    {
        if (applicationModel == null)
        {
            throw new IllegalArgumentException("applicationMode can't be null");
        }

        if (expressionType == null)
        {
            throw new IllegalArgumentException("expressionType can't be null");
        }

        if (contextDefinitions == null)
        {
            throw new IllegalArgumentException("contextDefinitions can't be null");
        }

        if (contextModel == null)
        {
            throw new IllegalArgumentException("contextModel can't be null");
        }

        if (typeAnalyzer == null)
        {
            throw new IllegalArgumentException("typeAnalyzer can't be null");
        }

        this.applicationModel = applicationModel;
        this.expressionType = expressionType;
        this.contextDefinitions = contextDefinitions;
        this.contextModel = contextModel;
        this.typeAnalyzer = typeAnalyzer;
    }


    public ExpressionType getExpressionType()
    {
        return expressionType;
    }


    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public ExpressionModelContext getContextModel()
    {
        return contextModel;
    }


    public Definitions getContextDefinitions()
    {
        return contextDefinitions;
    }


    public PropertyModel walk(ASTPropertyChain node, PropertyModel start, int startAt)
    {
        if (startAt <= 0)
        {
            throw new IllegalArgumentException("startAt must be greater than 0");
        }

        PropertyModel current = start;

        final int numChildren = node.jjtGetNumChildren();
        for (int i = startAt; i < numChildren; i++)
        {
            Object nextProp;
            Node kid;
            final Node chainLink = node.jjtGetChild(i);
            kid = chainLink.jjtGetChild(0);

            if (chainLink instanceof ASTPropertyChainDot)
            {
                if (kid instanceof ASTIdentifier)
                {
                    nextProp = ((ASTIdentifier) kid).getName();
                }
                else
                {
                    if (current.getType().equals(PropertyType.STATE) &&
                        kid instanceof ASTFunction &&
                        ((ASTFunction) kid).getName().equals(StateMachineValueTransformer.FROM)
                    )
                    {
                        return ExpressionUtil.BOOLEAN_TYPE;
                    }

                    // ignore other node types including ASTFunction
                    return null;
                }
            }
            else if (chainLink instanceof ASTPropertyChainSquare)
            {
                if (kid instanceof LiteralValueNode)
                {
                    nextProp = ((LiteralValueNode) kid).getLiteralValue();
                }
                else
                {
                    // make sure we analyze the sub expressions within []
                    typeAnalyzer.analyze(this, kid);

                    // .. give up on analyzing this node however
                    return null;
                }
            }
            else
            {
                throw new IllegalStateException("Invalid property chain link: " + chainLink);
            }


            if (nextProp == null)
            {
                return null;
            }

            final boolean isList = current.getType().equals(PropertyType.LIST);
            if ( current.getType().equals(PropertyType.MAP) || isList)
            {
                if (isList && nextProp.equals(LENGTH))
                {
                    current = ExpressionUtil.INTEGER_TYPE;
                }
                else
                {
                    final String elemTypeName = current.getTypeParam();
                    current = ExpressionUtil.getCollectionType(applicationModel, elemTypeName);
                }
            }
            else if ( current.getType().equals(PropertyType.DOMAIN_TYPE))
            {
                if (!(nextProp instanceof String))
                {
                    throw new InvalidExpressionException("Invalid poperty name: " + nextProp);
                }

                final String domainTypeName = current.getTypeParam();
                if (domainTypeName == null)
                {
                    return null;
                }

                final DomainType domainType = applicationModel.getDomainType(domainTypeName);
                if (domainType == null)
                {
                    throw new IllegalStateException("Domain type '" + domainTypeName + "' not found");
                }

                current = domainType.getProperty((String) nextProp);
            }
            else if (current.getType().equals(PropertyType.DATA_GRAPH))
            {
                return null;
            }
            else
            {
                throw new InvalidExpressionException("Cannot walk type '" + current.getType() + ": " + node);
            }

        }
        return current;
    }


    public TypeAnalyzerContext copy(ExpressionType type, ExpressionModelContext contextModel)
    {
        return new TypeAnalyzerContext(applicationModel, type, contextDefinitions, contextModel, typeAnalyzer);
    }


    public TypeAnalyzer getTypeAnalyzer()
    {
        return typeAnalyzer;
    }
}

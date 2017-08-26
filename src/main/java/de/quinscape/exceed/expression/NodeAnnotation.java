package de.quinscape.exceed.expression;

import de.quinscape.exceed.model.domain.PropertyModel;
import de.quinscape.exceed.runtime.js.CompilationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a set of values the exceed systems annotates AST expression nodes with.
 * <p>
 * <ul>
 * <li>A {@link CompilationResult} for ASTExpression nodes</li>
 * <li>A {@link CompilationResult} for ASTExpression nodes</li>
 * </ul>
 */
public class NodeAnnotation
{
    private final static Logger log = LoggerFactory.getLogger(NodeAnnotation.class);

    private final Node node;


    private CompilationResult compilationResult;

    private PropertyModel propertyType;


    public NodeAnnotation(Node node)
    {

        this.node = node;
    }


    public CompilationResult getCompilationResult()
    {
        return compilationResult;
    }


    public void setCompilationResult(CompilationResult compilationResult)
    {
        this.compilationResult = compilationResult;
    }


    public PropertyModel getPropertyType()
    {
        if (propertyType == null)
        {
            if (node instanceof LiteralValueNode)
            {
                return ((LiteralValueNode) node).getLiteralType();
            }
        }

        return propertyType;
    }


    public void setPropertyType(PropertyModel propertyType)
    {
        log.debug("set property type to {} on {}", propertyType, node);

        this.propertyType = propertyType;

    }
}

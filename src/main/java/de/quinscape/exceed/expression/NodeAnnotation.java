package de.quinscape.exceed.expression;

import de.quinscape.exceed.model.domain.property.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a set of values the exceed systems annotates AST expression nodes with.
 */
public class NodeAnnotation
{
    private final static Logger log = LoggerFactory.getLogger(NodeAnnotation.class);

    private final Node node;


    private String compilationResult;

    private String identifier;

    private PropertyModel propertyType;


    public NodeAnnotation(Node node)
    {

        this.node = node;
    }


    public String getCompilationResult()
    {
        return compilationResult;
    }


    public void setCompilationResult(String compilationResult)
    {
        this.compilationResult = compilationResult;
    }


    public String getIdentifier()
    {
        return identifier;
    }


    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
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

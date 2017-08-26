package de.quinscape.exceed.expression;

import de.quinscape.exceed.model.domain.PropertyModel;

/**
 * Implemented by nodes that represent literal values within an expression
 */
public interface LiteralValueNode
    extends Node
{
    Object getLiteralValue();
    PropertyModel getLiteralType();
}

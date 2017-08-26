package de.quinscape.exceed.expression;

/**
 * A node representation a named identifier or function.
 */
public interface ResolvableNode
    extends Node
{
    String getName();
}

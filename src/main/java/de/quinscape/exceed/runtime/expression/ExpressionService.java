package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.Identifier;
import de.quinscape.exceed.runtime.expression.Operation;

/**
 *  Evaluate a (partial) expression in a expression environment.
 *
 *  @see ExpressionOperations
 *  @see Operation
 *  @see Identifier
 */
public interface ExpressionService
{
    Object evaluate(Node node, ExpressionEnvironment env);
}

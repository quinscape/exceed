package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Identifier;
import de.quinscape.exceed.runtime.expression.annotation.Operation;

/**
 *  Evaluates a (partial) expression in a expression environment.
 *  <p>
 *      This interface is used where we don't transpile our expressions to Javascript but execute it directly to e.g.
 *      transform an exceed query expression into a JOOQ query expression.
 *  </p>
 *  <p>
 *      The expression service works based on annotated java classes that implement the operations and identifiers for that
 *      expression environment. The operations are function and method expressions in the expression environment. 
 *  </p>
 *  <p>
 *      The function <code>foo()</code> would be an operation without context object, while the <code>obj.bar()</code> method
 *      would be an operation that is applicable to <code>obj</code> instances of a certain type. This totally separates the
 *      objects the expression language acts upon from the syntax and implementation the expression language.
 *  </p>
 *
 *  @see ExpressionOperations
 *  @see Operation
 *  @see Identifier
 */
public interface ExpressionService
{
    Object evaluate(Node node, ExpressionEnvironment env);
}

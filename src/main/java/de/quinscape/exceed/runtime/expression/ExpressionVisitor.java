package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFloat;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTMult;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.SimpleNode;

public interface ExpressionVisitor<DI, DO>
{
    default DI visit(SimpleNode node, DO data)
    {
        throw new IllegalStateException("Generic visitor method should not be called");
    }

    DI visit(ASTExpression node, DO data);

    DI visit(ASTLogicalOr node, DO data);

    DI visit(ASTLogicalAnd node, DO data);

    DI visit(ASTEquality node, DO data);

    DI visit(ASTRelational node, DO data);

    DI visit(ASTAdd node, DO data);

    DI visit(ASTMult node, DO data);

    DI visit(ASTIdentifier node, DO data);

    DI visit(ASTPropertyChain node, DO data);

    DI visit(ASTFunction node, DO data);

    DI visit(ASTInteger node, DO data);

    DI visit(ASTString node, DO data);

    DI visit(ASTFloat node, DO data);

    DI visit(ASTBool node, DO data);

    DI visit(ASTNull node, DO data);
}

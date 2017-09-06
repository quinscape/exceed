package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.ASTArray;
import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTDecimal;
import de.quinscape.exceed.expression.ASTDiv;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTMult;
import de.quinscape.exceed.expression.ASTNegate;
import de.quinscape.exceed.expression.ASTNot;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.ASTPropertyChainSquare;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ASTSub;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.OperatorNode;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.expression.transform.DefaultTransformationContext;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

/**
 * Renders the abstract syntax tree of an expression into it's normal string form.
 * <p>
 * This class can be subclassed to implement Renders that transform the expression in some way  
 * </p>
 */
public class ExpressionRenderer
    implements ExpressionParserVisitor
{
    private final StringBuilder buf;

    public ExpressionRenderer()
    {
        this(new StringBuilder());
    }
    public ExpressionRenderer(StringBuilder buf)
    {
        this.buf = buf;
    }

    @Override
    public Object visit(SimpleNode node, Object data)
    {
        throw new ExceedRuntimeException("visit(SimpleNode,Object) should never be called. Do all AST classes implement the accept method?");
    }


    @Override
    public Object visit(ASTExpression node, Object data)
    {
        boolean isNestedExpression = node.jjtGetParent() != null;

        if (isNestedExpression)
        {
            buf.append('(');
        }

        final int numChildren = node.jjtGetNumChildren();
        for (int i=0; i < numChildren; i++)
        {
            visitChild(node.jjtGetChild(i), data);
        }
        if (isNestedExpression)
        {
            buf.append(')');
        }
        return data;
    }


    @Override
    public Object visit(ASTExpressionSequence node, Object data)
    {
        renderMultiBinary(node, "; ", data);
        return data;
    }


    @Override
    public Object visit(ASTAssignment node, Object data)
    {
        if (node.jjtGetNumChildren() != 2)
        {
            throw new IllegalStateException("Assignment operator must have exactly 2 arguments");
        }

        Node lft = node.jjtGetChild(0);
        Node rgt = node.jjtGetChild(1);

        visitChild(lft, data);
        buf.append(" = ");
        visitChild(rgt, data);
        return data;
    }

    protected Object renderMultiBinary(Node node, String op, Object data)
    {
        if (op == null)
        {
            if (node instanceof OperatorNode)
            {
                op = " " + ((OperatorNode) node).getOperator().getAsString() + " ";
            }
            else
            {
                throw new IllegalArgumentException(node + " is no operator node and no operator given.");
            }
        }
        
        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            if (i > 0)
            {
                buf.append(op);
            }

            final Node kid = node.jjtGetChild(i);
            visitChild(kid, data);
        }
        return data;
    }

    /**
     * Visits the given child node. This method as meant as central place for subclasses of expression render
     * to control invocation of child nodes. {@link DefaultTransformationContext.Renderer}
     *
     * @param kid       kid
     * @param data      data
     *
     * @return result of visit
     */
    protected Object visitChild(Node kid, Object data)
    {
        return kid.jjtAccept(this, data);
    }


    @Override
    public Object visit(ASTLogicalOr node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }

    @Override
    public Object visit(ASTLogicalAnd node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTEquality node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTRelational node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTAdd node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTSub node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTMult node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTDiv node, Object data)
    {
        return renderMultiBinary(node, null, data);
    }


    @Override
    public Object visit(ASTPropertyChain node, Object data)
    {
        final int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++)
        {
            final Node kid = node.jjtGetChild(i);
            visitChild(kid, data);
        }
        return data;
    }


    @Override
    public Object visit(ASTPropertyChainDot node, Object data)
    {
        buf.append(".");
        visitChild(node.jjtGetChild(0), data);
        return data;
    }


    @Override
    public Object visit(ASTPropertyChainSquare node, Object data)
    {
        buf.append("[");
        visitChild(node.jjtGetChild(0), data);
        buf.append("]");
        return data;
    }

//    @Override
//    public Object visit(ASTComputedPropertyChain node, Object data)
//    {
//        visitChild(node.jjtGetChild(0), data);
//        for (int i=1; i < node.jjtGetNumChildren(); i++)
//        {
//            buf.append("[");
//            visitChild(node.jjtGetChild(i), data);
//            buf.append("]");
//        }
//        return data;
//    }

    @Override
    public Object visit(ASTNot node, Object data)
    {
        buf.append("!");
        return visitChild(node.jjtGetChild(0), data);
    }


    @Override
    public Object visit(ASTNegate node, Object data)
    {
        buf.append("-");
        return visitChild(node.jjtGetChild(0), data);
    }


    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        buf.append(node.getName());
        return data;
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        buf.append(node.getName()).append('(');

        renderMultiBinary(node, ", ", data);

        buf.append(')');
        return data;
    }


    @Override
    public Object visit(ASTInteger node, Object data)
    {
        buf.append(node.getValue());
        return data;
    }


    @Override
    public Object visit(ASTString node, Object data)
    {
        buf.append(SingleQuoteJSONGenerator.INSTANCE.quote(node.getValue()));
        return data;
    }


    @Override
    public Object visit(ASTMap node, Object data)
    {
        buf.append('{');
        renderMultiBinary(node, ", ", data);
        buf.append('}');
        return data;
    }


    @Override
    public Object visit(ASTArray node, Object data)
    {
        buf.append('[');
        renderMultiBinary(node, ", ", data);
        buf.append(']');
        return data;
    }


    @Override
    public Object visit(ASTMapEntry node, Object data)
    {
        renderMultiBinary(node, ": ", data);
        return data;
    }


    @Override
    public Object visit(ASTDecimal node, Object data)
    {
        buf.append(node.getValue());
        return data;
    }


    @Override
    public Object visit(ASTBool node, Object data)
    {
        buf.append(node.getValue());
        return data;
    }


    @Override
    public Object visit(ASTNull node, Object data)
    {
        buf.append("null");
        return data;
    }


    public String getOutput()
    {
        return buf.toString();
    }


    /**
     * Convenience method to turn an ASTExpression back into a string
     *
     * @param n expression or partial expression
     * @return expression string
     */
    public static String render(Node n)
    {
        if (n == null)
        {
            return "<null>";
        }

        ExpressionRenderer renderer = new ExpressionRenderer();
        renderer.visitChild(n,null);
        return renderer.getOutput();
    }


    public StringBuilder getBuffer()
    {
        return buf;
    }
}

package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.ASTArray;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTComputedPropertyChain;
import de.quinscape.exceed.expression.ASTDiv;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFloat;
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
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ASTSub;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.OperatorNode;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.svenson.JSON;

/**
 * Renders the abstract syntax tree of an expression into it's normal string form.
 * <p>
 * This class can be subclassed to implement Renders that transform the expression in some way  
 * </p>
 */
public class ExpressionRenderer
    implements ExpressionParserVisitor
{
    
    protected StringBuilder buf = new StringBuilder();
    
    @Override
    public Object visit(SimpleNode node, Object data)
    {
        throw new ExceedRuntimeException("visit(SimpleNode,Object) should never be called. Do all AST classes implement the accept method?");
    }


    @Override
    public Object visit(ASTExpression node, Object data)
    {
        buf.append('(');
        node.childrenAccept(this, data);
        buf.append(')');
        return data;
    }


//    @Override
//    public Object visit(ASTAssignment node, Object data)
//    {
//        Node lft = node.jjtGetChild(0);
//        Node rgt = node.jjtGetChild(1);
//
//        lft.jjtAccept(this, data);
//        buf.append(" = ");
//        rgt.jjtAccept(this, data);
//        return data;
//    }


    private Object renderMultiBinary(Node node, String op, Object data)
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
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
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
        return renderMultiBinary(node, ".", data);
    }


    @Override
    public Object visit(ASTComputedPropertyChain node, Object data)
    {
        node.jjtGetChild(0).jjtAccept(this, data);
        for (int i=1; i < node.jjtGetNumChildren(); i++)
        {
            buf.append("[");
            node.jjtGetChild(i).jjtAccept(this, data);
            buf.append("]");
        }
        return data;
    }


    @Override
    public Object visit(ASTNot node, Object data)
    {
        buf.append("!");
        return node.jjtGetChild(0).jjtAccept(this, data);
    }


    @Override
    public Object visit(ASTNegate node, Object data)
    {
        buf.append("-");
        return node.jjtGetChild(0).jjtAccept(this, data);
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
        buf.append(JSON.defaultJSON().quote(node.getValue()));
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
    public Object visit(ASTFloat node, Object data)
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
        ExpressionRenderer renderer = new ExpressionRenderer();
        n.jjtAccept(renderer, null);
        return renderer.getOutput();
    }
}

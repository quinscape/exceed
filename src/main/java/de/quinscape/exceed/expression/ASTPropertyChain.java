/* Generated By:JJTree: Do not edit this line. ASTPropertyChain.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,
NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

import de.quinscape.exceed.runtime.model.ExpressionRenderer;

public class ASTPropertyChain
    extends SimpleNode
{
    public ASTPropertyChain(int id)
    {
        super(id);
    }


    public ASTPropertyChain(ExpressionParser p, int id)
    {
        super(p, id);
    }


    /**
     * Accept the visitor.
     **/
    public Object jjtAccept(ExpressionParserVisitor visitor, Object data)
    {

        return
            visitor.visit(this, data);
    }

    public Node getChainChild(int index)
    {
        final Node link = jjtGetChild(index);
        if (index == 0)
        {
            return link;
        }
        else
        {
            return link.jjtGetChild(0);
        }
    }


    @Override
    public String toString()
    {
        return super.toString() + " " + ExpressionRenderer.render(this);
    }
}
/* JavaCC - OriginalChecksum=142d79af8dafd11ed964e736410e6a12 (do not edit this line) */

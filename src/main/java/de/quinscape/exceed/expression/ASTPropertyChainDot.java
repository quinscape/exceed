/* Generated By:JJTree: Do not edit this line. ASTPropertyChainDot.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,
NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

public class ASTPropertyChainDot
    extends SimpleNode
    implements PropertyChainLink
{
    public ASTPropertyChainDot(int id)
    {
        super(id);
    }


    public ASTPropertyChainDot(ExpressionParser p, int id)
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


    @Override
    public String toString()
    {
        return super.toString() + " " + jjtGetChild(0).toString();
    }
}
/* JavaCC - OriginalChecksum=c677bd223143c9745c6da4bbfb38e4b7 (do not edit this line) */

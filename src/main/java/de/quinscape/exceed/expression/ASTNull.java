/* Generated By:JJTree: Do not edit this line. ASTNull.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,
NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

import de.quinscape.exceed.model.domain.PropertyModel;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class ASTNull
    extends SimpleNode
    implements LiteralValueNode
{

    public ASTNull(int id)
    {
        super(id);
    }


    public ASTNull(ExpressionParser p, int id)
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
    public Object getLiteralValue()
    {
        return null;
    }


    @Override
    public PropertyModel getLiteralType()
    {
        return ExpressionUtil.OBJECT_TYPE;
    }
}
/* JavaCC - OriginalChecksum=f059d4af1a429e6dcf956bf77c3dc553 (do not edit this line) */

/* Generated By:JJTree: Do not edit this line. ASTBool.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,
NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class ASTBool
    extends SimpleNode
    implements LiteralValueNode
{

    private boolean value;


    public ASTBool(int id)
    {
        super(id);
    }


    public ASTBool(ExpressionParser p, int id)
    {
        super(p, id);
    }


    /**
     * Accept the visitor.
     **/
    public Object jjtAccept(ExpressionParserVisitor visitor, Object data)
    {

        return visitor.visit(this, data);
    }


    public boolean getValue()
    {
        return value;
    }


    public void setValue(boolean value)
    {
        this.value = value;
    }


    @Override
    public Object getLiteralValue()
    {
        return value;
    }


    @Override
    public PropertyModel getLiteralType()
    {
        return ExpressionUtil.BOOLEAN_TYPE;
    }
}
/* JavaCC - OriginalChecksum=cc65222125b74cbe4a2315f8dd49d1ac (do not edit this line) */

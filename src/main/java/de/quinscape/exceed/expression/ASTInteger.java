/* Generated By:JJTree: Do not edit this line. ASTInteger.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,
NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

import de.quinscape.exceed.model.domain.PropertyModel;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class ASTInteger
    extends SimpleNode
    implements LiteralValueNode
{
    public ASTInteger(int id)
    {
        super(id);
    }


    public ASTInteger(ExpressionParser p, int id)
    {
        super(p, id);
    }


    private int value;


    /**
     * Accept the visitor.
     **/
    public Object jjtAccept(ExpressionParserVisitor visitor, Object data)
    {

        return visitor.visit(this, data);
    }


    @Override
    public String toString()
    {
        return super.toString() + " " + value;
    }


    public int getValue()
    {
        return value;
    }


    public void setValue(int value)
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
        return ExpressionUtil.INTEGER_TYPE;
    }
}
/* JavaCC - OriginalChecksum=609b15f3bbfd3da2b3f65f127c8c5957 (do not edit this line) */

/* Generated By:JJTree: Do not edit this line. ASTNegate.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

public
class ASTNegate extends SimpleNode {
  public ASTNegate(int id) {
    super(id);
  }

  public ASTNegate(ExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=614be5d956268d592bbfb3c27ef2974e (do not edit this line) */

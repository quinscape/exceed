/* Generated By:JJTree: Do not edit this line. ASTAssignment.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

public
class ASTAssignment extends SimpleNode {
  public ASTAssignment(int id) {
    super(id);
  }

  public ASTAssignment(ExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e8a2e4be3db4b7d938e1e94783e170df (do not edit this line) */
/* Generated By:JJTree: Do not edit this line. ASTRelational.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

public
class ASTRelational extends ComparatorNode {
  public ASTRelational(int id) {
    super(id);
  }

  public ASTRelational(ExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {

    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=8149bfe701e571021922d6836d344529 (do not edit this line) */

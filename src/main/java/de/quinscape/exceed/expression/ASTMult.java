/* Generated By:JJTree: Do not edit this line. ASTMult.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

public
class ASTMult extends OperatorNode {
  public ASTMult(int id) {
    super(id);
    setOperator(Operator.MULTIPLY);

  }

  public ASTMult(ExpressionParser p, int id) {
    super(p, id);
    setOperator(Operator.MULTIPLY);

  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=ba0f2cecfa528353afedcf6a457d35ac (do not edit this line) */

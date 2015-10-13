/* Generated By:JavaCC: Do not edit this line. ExpressionParserDefaultVisitor.java Version 6.0_1 */
package de.quinscape.exceed.expression;

public class ExpressionParserDefaultVisitor implements ExpressionParserVisitor{
  public Object defaultVisit(SimpleNode node, Object data){
    node.childrenAccept(this, data);
    return data;
  }
  public Object visit(SimpleNode node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTExpression node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTLogicalOr node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTLogicalAnd node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTEquality node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTRelational node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTAdd node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTSub node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTMult node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTDiv node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTPropertyChain node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTIdentifier node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTFunction node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTInteger node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTString node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTFloat node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTBool node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTNull node, Object data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=66be610b50c2de2cbfda4fc648949590 (do not edit this line) */

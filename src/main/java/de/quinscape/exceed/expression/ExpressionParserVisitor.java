/* Generated By:JavaCC: Do not edit this line. ExpressionParserVisitor.java Version 6.0_1 */
package de.quinscape.exceed.expression;

public interface ExpressionParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTExpression node, Object data);
  public Object visit(ASTExpressionSequence node, Object data);
  public Object visit(ASTAssignment node, Object data);
  public Object visit(ASTLogicalOr node, Object data);
  public Object visit(ASTLogicalAnd node, Object data);
  public Object visit(ASTEquality node, Object data);
  public Object visit(ASTRelational node, Object data);
  public Object visit(ASTAdd node, Object data);
  public Object visit(ASTSub node, Object data);
  public Object visit(ASTMult node, Object data);
  public Object visit(ASTDiv node, Object data);
  public Object visit(ASTNot node, Object data);
  public Object visit(ASTNegate node, Object data);
  public Object visit(ASTPropertyChain node, Object data);
  public Object visit(ASTPropertyChainDot node, Object data);
  public Object visit(ASTPropertyChainSquare node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTFunction node, Object data);
  public Object visit(ASTInteger node, Object data);
  public Object visit(ASTString node, Object data);
  public Object visit(ASTMap node, Object data);
  public Object visit(ASTArray node, Object data);
  public Object visit(ASTMapEntry node, Object data);
  public Object visit(ASTDecimal node, Object data);
  public Object visit(ASTBool node, Object data);
  public Object visit(ASTNull node, Object data);
}
/* JavaCC - OriginalChecksum=544ea8258b9874a69c7b01426b2d3d82 (do not edit this line) */

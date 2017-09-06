/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package de.quinscape.exceed.expression;

import java.util.function.Consumer;

public
class SimpleNode implements Node {

  protected Node parent;
  protected Node[] children;
  protected int id;
  protected NodeAnnotation value;
  protected ExpressionParser parser;

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(ExpressionParser p, int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  public void jjtSetValue(NodeAnnotation value) { this.value = value; }
  public NodeAnnotation jjtGetValue() { return value; }

  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data)
{
    return visitor.visit(this, data);
  }

  /** Accept the visitor. **/
  public Object childrenAccept(ExpressionParserVisitor visitor, Object data)
{
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        children[i].jjtAccept(visitor, data);
      }
    }
    return data;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() {
    return ExpressionParserTreeConstants.jjtNodeName[id];
  }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump( String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode)children[i];
        if (n != null) {
          n.dump(prefix + " ");
        }
      }
    }
  }

  public StringBuilder dump(StringBuilder buf, String prefix) {

    buf.append(toString(prefix)).append('\n');
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode)children[i];
        if (n != null) {
          n.dump(buf, prefix + " ");
        }
      }
    }
    return buf;
  }

  public int getId() {
    return id;
  }

  public NodeAnnotation annotation()
  {
    if (value == null)
    {
      value = new NodeAnnotation(this);
    }
    return value;
  }


  /**
   * Recursively walks over the node structure and calls the consumer for every node.
   *
   * @param consumer  consumer
   */
  public void walk(Consumer<Node> consumer)
  {
    consumer.accept(this);

    final int len = jjtGetNumChildren();
    for (int i = 0; i < len; i++)
    {
      this.jjtGetChild(i).walk(consumer);
    }
  }
}

/* JavaCC - OriginalChecksum=5e30f8ff5804b3805a63702cc4000a00 (do not edit this line) */

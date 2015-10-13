/* ExpressionParser.java */
/* Generated By:JJTree&JavaCC: Do not edit this line. ExpressionParser.java */
package de.quinscape.exceed.expression;

import de.quinscape.exceed.expression.ExpressionParserConstants;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.Util;
import java.io.Reader;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the exceed expression language in a tree of AST nodes.
 *
 * generated by the grammar in src/main/resources/Expression.jjt
 *
 */
public class ExpressionParser/*@bgen(jjtree)*/implements ExpressionParserTreeConstants, ExpressionParserConstants {/*@bgen(jjtree)*/
  protected JJTExpressionParserState jjtree = new JJTExpressionParserState();private static Logger log = LoggerFactory.getLogger(ExpressionParser.class);

    public static ASTExpression parse(String expression) throws ParseException
    {
        return parse(new StringReader(expression));
    }


    public static ASTExpression parse(Reader reader) throws ParseException
    {
        ExpressionParser t = new ExpressionParser(reader);
        ASTExpression expression = t.Expression();

        Token next = t.token.next;
        if (next.kind != ExpressionParserConstants.EOF)
        {
            throw t.generateParseException();
        }
        return expression;
    }

/** Expression root production. */
  final public ASTExpression Expression() throws ParseException {/*@bgen(jjtree) Expression */
  ASTExpression jjtn000 = new ASTExpression(JJTEXPRESSION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Expr();
jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
{if ("" != null) return jjtn000;}
    } catch (Throwable jjte000) {
if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

/** Expression. */
  final public void Expr() throws ParseException {
    OrExpression();
  }

/** An or expression. */
  final public void OrExpression() throws ParseException {
ASTLogicalOr jjtn001 = new ASTLogicalOr(JJTLOGICALOR);
      boolean jjtc001 = true;
      jjtree.openNodeScope(jjtn001);
    try {
      AndExpression();
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 20:{
          ;
          break;
          }
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        jj_consume_token(20);
        AndExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
        jjtree.clearNodeScope(jjtn001);
        jjtc001 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte001 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte001;}
      }
      if (jjte001 instanceof ParseException) {
        {if (true) throw (ParseException)jjte001;}
      }
      {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
        jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
      }
    }
  }

/** An and expression. */
  final public void AndExpression() throws ParseException {
ASTLogicalAnd jjtn001 = new ASTLogicalAnd(JJTLOGICALAND);
      boolean jjtc001 = true;
      jjtree.openNodeScope(jjtn001);
    try {
      EqualityExpression();
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 21:{
          ;
          break;
          }
        default:
          jj_la1[1] = jj_gen;
          break label_2;
        }
        jj_consume_token(21);
        EqualityExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
        jjtree.clearNodeScope(jjtn001);
        jjtc001 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte001 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte001;}
      }
      if (jjte001 instanceof ParseException) {
        {if (true) throw (ParseException)jjte001;}
      }
      {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
        jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
      }
    }
  }

/** An equality Expression. */
  final public void EqualityExpression() throws ParseException {Token op = null;
ASTEquality jjtn001 = new ASTEquality(JJTEQUALITY);
      boolean jjtc001 = true;
      jjtree.openNodeScope(jjtn001);
    try {
      RelationalExpression();
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 22:
      case 23:{
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 22:{
          op = jj_consume_token(22);
          break;
          }
        case 23:{
          op = jj_consume_token(23);
          break;
          }
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        RelationalExpression();
        break;
        }
      default:
        jj_la1[3] = jj_gen;
        ;
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
        jjtree.clearNodeScope(jjtn001);
        jjtc001 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte001 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte001;}
      }
      if (jjte001 instanceof ParseException) {
        {if (true) throw (ParseException)jjte001;}
      }
      {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
        jjtree.closeNodeScope(jjtn001,  jjtree . nodeArity ( ) == 2);
      }
    }
if (jjtree.nodeCreated() && op != null)
{
            if (op.image.equals("=="))
            {
                ((OperatorNode)jjtree.peekNode()).setOperator(Operator.EQUALS);
}
            else if (op.image.equals("!="))
{
                ((OperatorNode)jjtree.peekNode()).setOperator(Operator.NOT_EQUALS);
            }
        }
  }

/** An relational Expression. */
  final public void RelationalExpression() throws ParseException {Token op = null;
ASTRelational jjtn001 = new ASTRelational(JJTRELATIONAL);
      boolean jjtc001 = true;
      jjtree.openNodeScope(jjtn001);
    try {
      AdditionExpression();
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 24:
      case 25:
      case 26:
      case 27:{
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 24:{
          op = jj_consume_token(24);
          break;
          }
        case 25:{
          op = jj_consume_token(25);
          break;
          }
        case 26:{
          op = jj_consume_token(26);
          break;
          }
        case 27:{
          op = jj_consume_token(27);
          break;
          }
        default:
          jj_la1[4] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        AdditionExpression();
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        ;
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
        jjtree.clearNodeScope(jjtn001);
        jjtc001 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte001 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte001;}
      }
      if (jjte001 instanceof ParseException) {
        {if (true) throw (ParseException)jjte001;}
      }
      {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
        jjtree.closeNodeScope(jjtn001,  jjtree . nodeArity ( ) == 2);
      }
    }
if (jjtree.nodeCreated() && op != null)
        {
            if (op.image.equals("<"))
            {
                ((OperatorNode)jjtree.peekNode()).setOperator(Operator.LESS);
            }
            else if (op.image.equals("<="))
            {
                ((OperatorNode)jjtree.peekNode()).setOperator(Operator.LESS_OR_EQUALS);
            }
            else if (op.image.equals(">"))
            {
                ((OperatorNode)jjtree.peekNode()).setOperator(Operator.GREATER);
            }
            else if (op.image.equals(">="))
            {
                ((OperatorNode)jjtree.peekNode()).setOperator(Operator.GREATER_OR_EQUALS);
            }
        }
  }

/** An Additive Expression. */
  final public void AdditionExpression() throws ParseException {
ASTAdd jjtn001 = new ASTAdd(JJTADD);
    boolean jjtc001 = true;
    jjtree.openNodeScope(jjtn001);
    try {
      SubtractionExpression();
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 28:{
          ;
          break;
          }
        default:
          jj_la1[6] = jj_gen;
          break label_3;
        }
        jj_consume_token(28);
        SubtractionExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
      jjtree.clearNodeScope(jjtn001);
      jjtc001 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte001 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte001;}
    }
    if (jjte001 instanceof ParseException) {
      {if (true) throw (ParseException)jjte001;}
    }
    {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
      jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
    }
    }
  }

/** An Additive Expression. */
  final public void SubtractionExpression() throws ParseException {
ASTSub jjtn001 = new ASTSub(JJTSUB);
      boolean jjtc001 = true;
      jjtree.openNodeScope(jjtn001);
    try {
      MultiplicationExpression();
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 29:{
          ;
          break;
          }
        default:
          jj_la1[7] = jj_gen;
          break label_4;
        }
        jj_consume_token(29);
        MultiplicationExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
        jjtree.clearNodeScope(jjtn001);
        jjtc001 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte001 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte001;}
      }
      if (jjte001 instanceof ParseException) {
        {if (true) throw (ParseException)jjte001;}
      }
      {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
        jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
      }
    }
  }

/** A Multiplicative Expression. */
  final public void MultiplicationExpression() throws ParseException {
ASTMult jjtn001 = new ASTMult(JJTMULT);
    boolean jjtc001 = true;
    jjtree.openNodeScope(jjtn001);
    try {
      DivisionExpression();
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 30:{
          ;
          break;
          }
        default:
          jj_la1[8] = jj_gen;
          break label_5;
        }
        jj_consume_token(30);
        DivisionExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
      jjtree.clearNodeScope(jjtn001);
      jjtc001 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte001 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte001;}
    }
    if (jjte001 instanceof ParseException) {
      {if (true) throw (ParseException)jjte001;}
    }
    {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
      jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
    }
    }
  }

/** A Multiplicative Expression. */
  final public void DivisionExpression() throws ParseException {
ASTDiv jjtn001 = new ASTDiv(JJTDIV);
    boolean jjtc001 = true;
    jjtree.openNodeScope(jjtn001);
    try {
      PropertyChainExpression();
      label_6:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 31:{
          ;
          break;
          }
        default:
          jj_la1[9] = jj_gen;
          break label_6;
        }
        jj_consume_token(31);
        PropertyChainExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
      jjtree.clearNodeScope(jjtn001);
      jjtc001 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte001 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte001;}
    }
    if (jjte001 instanceof ParseException) {
      {if (true) throw (ParseException)jjte001;}
    }
    {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
      jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
    }
    }
  }

/** A Property Access. */
  final public void PropertyChainExpression() throws ParseException {
ASTPropertyChain jjtn001 = new ASTPropertyChain(JJTPROPERTYCHAIN);
      boolean jjtc001 = true;
      jjtree.openNodeScope(jjtn001);
    try {
      UnaryExpression();
      label_7:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 32:{
          ;
          break;
          }
        default:
          jj_la1[10] = jj_gen;
          break label_7;
        }
        jj_consume_token(32);
        UnaryExpression();
      }
    } catch (Throwable jjte001) {
if (jjtc001) {
        jjtree.clearNodeScope(jjtn001);
        jjtc001 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte001 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte001;}
      }
      if (jjte001 instanceof ParseException) {
        {if (true) throw (ParseException)jjte001;}
      }
      {if (true) throw (Error)jjte001;}
    } finally {
if (jjtc001) {
        jjtree.closeNodeScope(jjtn001, jjtree.nodeArity() > 1);
      }
    }
  }

/** A Unary Expression. */
  final public void UnaryExpression() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 33:{
      jj_consume_token(33);
      Expression();
      jj_consume_token(34);
      break;
      }
    default:
      jj_la1[11] = jj_gen;
      if (jj_2_1(2)) {
        Function();
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case IDENTIFIER:{
          Identifier();
          break;
          }
        case BOOLEAN_LITERAL:{
          Bool();
          break;
          }
        case INTEGER_LITERAL:{
          Integer();
          break;
          }
        case FLOATING_POINT_LITERAL:{
          Float();
          break;
          }
        case SQUOTED_STRING:{
          String();
          break;
          }
        case NULL:{
          Null();
          break;
          }
        default:
          jj_la1[12] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
  }

/** An Identifier. */
  final public void Identifier() throws ParseException {/*@bgen(jjtree) Identifier */
  ASTIdentifier jjtn000 = new ASTIdentifier(JJTIDENTIFIER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(IDENTIFIER);
jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
jjtn000.setName(t.image);
    } finally {
if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

/** A function call. */
  final public void Function() throws ParseException {/*@bgen(jjtree) Function */
ASTFunction jjtn000 = new ASTFunction(JJTFUNCTION);
boolean jjtc000 = true;
jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(IDENTIFIER);
      jj_consume_token(33);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NULL:
      case BOOLEAN_LITERAL:
      case INTEGER_LITERAL:
      case FLOATING_POINT_LITERAL:
      case IDENTIFIER:
      case SQUOTED_STRING:
      case 33:{
        Expr();
        label_8:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case 35:{
            ;
            break;
            }
          default:
            jj_la1[13] = jj_gen;
            break label_8;
          }
          jj_consume_token(35);
          Expr();
        }
        break;
        }
      default:
        jj_la1[14] = jj_gen;
        ;
      }
      jj_consume_token(34);
jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
jjtn000.setName(t.image);
    } catch (Throwable jjte000) {
if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

/** An Integer. */
  final public void Integer() throws ParseException {/*@bgen(jjtree) Integer */
ASTInteger jjtn000 = new ASTInteger(JJTINTEGER);
boolean jjtc000 = true;
jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(INTEGER_LITERAL);
jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
jjtn000.setValue(Integer.parseInt(t.image));
    } finally {
if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

/** An String literal. */
  final public void String() throws ParseException {/*@bgen(jjtree) String */
    ASTString jjtn000 = new ASTString(JJTSTRING);
    boolean jjtc000 = true;
    jjtree.openNodeScope(jjtn000);Token t;
    try {
      //<DQUOTED_STRING> | <SQUOTED_STRING>
          t = jj_consume_token(SQUOTED_STRING);
jjtree.closeNodeScope(jjtn000, true);
      jjtc000 = false;
jjtn000.setValue(Util.parseSingleQuotedString(t.image));
    } finally {
if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

/** An Floating point number literal. */
  final public void Float() throws ParseException {/*@bgen(jjtree) Float */
    ASTFloat jjtn000 = new ASTFloat(JJTFLOAT);
    boolean jjtc000 = true;
    jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(FLOATING_POINT_LITERAL);
jjtree.closeNodeScope(jjtn000, true);
      jjtc000 = false;
jjtn000.setValue(Double.valueOf(t.image));
    } finally {
if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

/** An boolean literal. */
  final public void Bool() throws ParseException {/*@bgen(jjtree) Bool */
    ASTBool jjtn000 = new ASTBool(JJTBOOL);
    boolean jjtc000 = true;
    jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(BOOLEAN_LITERAL);
jjtree.closeNodeScope(jjtn000, true);
      jjtc000 = false;
jjtn000.setValue(Boolean.valueOf(t.image));
    } finally {
if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

/** A null literal. */
  final public void Null() throws ParseException {/*@bgen(jjtree) Null */
  ASTNull jjtn000 = new ASTNull(JJTNULL);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(NULL);
    } finally {
if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  private boolean jj_2_1(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_3R_9()
 {
    if (jj_scan_token(IDENTIFIER)) return true;
    if (jj_scan_token(33)) return true;
    return false;
  }

  private boolean jj_3_1()
 {
    if (jj_3R_9()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public ExpressionParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[15];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x100000,0x200000,0xc00000,0xc00000,0xf000000,0xf000000,0x10000000,0x20000000,0x40000000,0x80000000,0x0,0x0,0x48b80,0x0,0x48b80,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x1,0x2,0x0,0x8,0x2,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public ExpressionParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ExpressionParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ExpressionParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public ExpressionParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ExpressionParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public ExpressionParser(ExpressionParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(ExpressionParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  @SuppressWarnings("serial")
  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[36];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 15; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 36; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}

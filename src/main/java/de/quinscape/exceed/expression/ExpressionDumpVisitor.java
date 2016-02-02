package de.quinscape.exceed.expression;/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This is an example of how the Visitor pattern might be used to
 * implement the dumping code that comes with SimpleNode.  It's a bit
 * long-winded, but it does illustrate a couple of the main points.
 * <ol>
 * <li> the visitor can maintain state between the nodes that it visits
 * (for example the current indentation level).
 * </li>
 * <p>
 * <li>if you don't implement a jjtAccept() method for a subclass of
 * SimpleNode, then SimpleNode's acceptor will get called.
 * </li>
 * <li> the utility method childrenAccept() can be useful when
 * implementing preorder or postorder tree walks.
 * </li>
 * </ol>
 */

public class ExpressionDumpVisitor
    implements ExpressionParserVisitor
{
    private int indent = 0;

    protected StringBuilder buf = new StringBuilder();


    protected void indent()
    {
        for (int i = 0; i < indent; ++i)
        {
            buf.append(' ');
        }
    }


    public Object visit(SimpleNode node, Object data)
    {
        indent();
        buf.append(node).append(": acceptor not unimplemented in subclass?");
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    public Object visit(ASTExpression node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTLogicalOr node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTLogicalAnd node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTEquality node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTRelational node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    public Object visit(ASTAdd node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTSub node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    public Object visit(ASTMult node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTDiv node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    public Object visit(ASTIdentifier node, Object data)
    {
        indent();
        buf.append(node).append(" ").append(node.getName());
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTPropertyChain node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTComputedPropertyChain node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTNot node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTNegate node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        indent();
        buf.append(node).append(" ").append(node.getName());
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    public Object visit(ASTInteger node, Object data)
    {
        indent();
        buf.append(node).append(node.getValue());
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTString node, Object data)
    {
        indent();
        buf.append(node).append(node.getValue());
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTMap node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTMapEntry node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;

        return data;
    }


    @Override
    public Object visit(ASTFloat node, Object data)
    {
        indent();
        buf.append(node).append(node.getValue());
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }


    @Override
    public Object visit(ASTBool node, Object data)
    {
        indent();
        buf.append(node).append(node.getValue());
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }


    @Override
    public Object visit(ASTNull node, Object data)
    {
        indent();
        buf.append(node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }


    public String getContent()
    {
        return buf.toString();
    }
}

/*end*/

package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

public class ServerSideComponentExpressionTransform
    implements ExpressionTransformer
{
    public final static ServerSideComponentExpressionTransform INSTANCE = new ServerSideComponentExpressionTransform();

    private ServerSideComponentExpressionTransform()
    {
    }

    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        if (node instanceof ASTPropertyChain)
        {
            final Node kid = node.jjtGetChild(0);
            return kid instanceof ASTIdentifier && ((ASTIdentifier) kid).getName().equals("props");
        }

        return false;
    }

    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {

        if ((node.jjtGetNumChildren() != 2))
        {
            throw new InvalidExpressionException("Invalid props property chain: " + ExpressionUtil.renderExpressionOf(node));
        }

        final Node second = node.jjtGetChild(1).jjtGetChild(0);
        if (!(second instanceof ASTIdentifier))
        {
            throw new InvalidExpressionException("Invalid props property chain: " + ExpressionUtil.renderExpressionOf(second));
        }

        String name = ((ASTIdentifier) second).getName();
        ctx.output("_attrs[" + SingleQuoteJSONGenerator.INSTANCE.forValue(name) + "]");
    }
}

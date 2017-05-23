package de.quinscape.exceed.runtime.service.client.scope;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.expression.ExpressionValue;

import java.util.HashSet;
import java.util.Set;

public class TranslationReferenceVisitor
    extends ExpressionParserDefaultVisitor
{
    private final Set<String> references;


    public TranslationReferenceVisitor()
    {
        references = new HashSet<>();
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        String functionName = node.getName();

        if (functionName.equals("i18n"))
        {
            Node n = node.jjtGetChild(0);
            if (!(n instanceof ASTString))
            {
                throw new IllegalStateException("First argument for i18n() is not a string literal");
            }

            String code = ((ASTString) n).getValue();
            references.add(code);
        }
        return null;
    }


    public Set<String> getReferences()
    {
        return references;
    }


    public void visit(ExpressionValue attributeValue)
    {
        if (attributeValue != null)
        {
            final ASTExpression astExpression = attributeValue.getAstExpression();
            if (astExpression != null)
            {
                this.visit(astExpression, null);
            }
        }
    }
}

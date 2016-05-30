package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.action.ClientActionRenderer;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

public class SyslogCallGenerator
    implements ClientActionRenderer
{
    @Override
    public void renderJsCode(View view, ExpressionRenderer renderer, ASTFunction node)
    {

        StringBuilder buf = renderer.getBuffer();
        buf.append("_a.action({ action: 'syslog', args:[");

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            if (i > 0)
                buf.append(", ");
            node.jjtGetChild(i).jjtAccept(renderer, null);
        }

        buf.append("]})");

    }
}

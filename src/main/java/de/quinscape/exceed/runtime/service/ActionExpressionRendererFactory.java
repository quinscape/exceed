package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.action.ClientActionRenderer;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import org.svenson.JSON;

import java.util.HashMap;
import java.util.Map;

public class ActionExpressionRendererFactory
{
    private final Map<String,ClientActionRenderer> actionCallGenerators;
    public ActionExpressionRendererFactory(ActionService actionService)
    {
        actionCallGenerators = collectGeneratorsFromRegisteredActions(actionService);

        // client actions available in all contexts
        actionCallGenerators.put("update", (view, renderer, node) -> {

            if (node.jjtGetNumChildren() != 1 && node.jjtGetNumChildren() != 2)
            {
                throw new InvalidClientExpressionException("update() must have one or two parameters (id, [vars])");
            }
            StringBuilder buf = renderer.getBuffer();
            buf.append("_a.update(");

            node.jjtGetChild(0).jjtAccept(renderer, null);

            if (node.jjtGetNumChildren() == 2)
            {
                buf.append(", ");
                node.jjtGetChild(1).jjtAccept(renderer, null);
            }

            buf.append(")");
        });

        actionCallGenerators.put("action", (view, renderer, node) -> {

            if (node.jjtGetNumChildren() != 1)
            {
                throw new InvalidClientExpressionException("action() needs one parameter (actionModel)");
            }

            StringBuilder buf = renderer.getBuffer();
            buf.append("_a.action(");
            node.jjtGetChild(0).jjtAccept(renderer, null);
            buf.append(")");
        });

    }

    private Map<String, ClientActionRenderer> collectGeneratorsFromRegisteredActions(ActionService actionService)
    {
        Map<String, ClientActionRenderer> map = new HashMap<>();

        final ClientActionRenderer defaultGenerator = (view, renderer, node) -> {
            if (node.jjtGetNumChildren() != 1)
            {
                throw new InvalidClientExpressionException( node.getName() + "() takes one parameter (model)");
            }

            StringBuilder buf = renderer.getBuffer();
            buf.append("_v.action( ");
            node.jjtGetChild(0).jjtAccept(renderer, null);
            buf.append(", ")
                .append(JSON.defaultJSON().quote(node.getName()))
                .append(" )");
        };

        for (String name : actionService.getActionNames())
        {
            Action action = actionService.getAction(name);

            if (action instanceof ClientActionRenderer)
            {
                map.put(name, (ClientActionRenderer)action);
            }
            else
            {
                map.put(name, defaultGenerator);
            }
        }

        return map;
    }

    public ActionExpressionRenderer create(Map<String,ClientActionRenderer> extraGenerators)
    {

        Map<String, ClientActionRenderer> generators = new HashMap<>(actionCallGenerators);
        generators.putAll(extraGenerators);

        return new ActionExpressionRenderer(generators);
    }


}

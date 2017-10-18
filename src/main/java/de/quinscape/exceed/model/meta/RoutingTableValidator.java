package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

import java.util.Map;

public class RoutingTableValidator
    implements ModelValidator
{
    @Override
    public void validate(ModelValidationContext ctx, ApplicationModel applicationModel)
    {
        final RoutingTable routingTable = applicationModel.getRoutingTable();

        if (routingTable == null)
        {
            return;
        }

        for (Map.Entry<String, Mapping> e : routingTable.getMappings().entrySet())
        {
            final String location = e.getKey();
            final Mapping mapping = e.getValue();
            final String viewName = mapping.getViewName();
            if (viewName != null)
            {
                if (!applicationModel.getViews().containsKey(viewName))
                {
                    ctx.registerError(new ExpressionModelContext(routingTable), null, "Routing location " + location + " references non-existing view '" + viewName + "'");
                }
            }

            final String processName = mapping.getProcessName();
            if (processName != null)
            {
                if (!applicationModel.getProcesses().containsKey(processName))
                {
                    ctx.registerError(new ExpressionModelContext(routingTable), null, "Routing location " + location + " references non-existing process '" + processName + "'");
                }
            }
        }
    }
}

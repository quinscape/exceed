package de.quinscape.exceed.runtime.service.client.provider.editor;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.model.translation.TranslationRuleLocation;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@ExceedEditorProvider
public class TranslationRuleLocationsProvider
    implements ClientStateProvider
{

    @Override
    public String getName()
    {
        return "translationRuleLocations";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.APPLICATION;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
        Exception
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        List<TranslationRuleLocation> locations = new ArrayList<>();
        for (String processName : applicationModel.getProcesses().keySet())
        {
            locations.add(new TranslationRuleLocation(processName, null));
        }

        for (View view : applicationModel.getViews().values())
        {
            if (view.isContainedInProcess())
            {
                locations.add(new TranslationRuleLocation(view.getProcessName(), view.getLocalName()));
            }
            else
            {
                locations.add(new TranslationRuleLocation(null, view.getName()));
            }
        }

        locations.sort(TranslationRuleLocationComparator.INSTANCE);

        return new DefaultClientData(locations);
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}

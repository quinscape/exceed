package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.runtime.component.QueryError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates intermediary data results from the component queries and component query errors, also the registered translation keys for the
 * current component tree
 */
public class ViewData
{
    private final Map<String, ComponentData> componentData;

    private final Set<String> translations;

    private Map<String, List<QueryError>> errors;

    public ViewData()
    {
        componentData = new HashMap<>();
        translations = new HashSet<>();
        errors = new HashMap<>();
    }

    public void provide(String componentId, ComponentData componentData)
    {
        this.componentData.put(componentId, componentData);

        for (QueryError error : componentData.getErrors())
        {
            List<QueryError> errorList = errors.get(componentId);

            if (errorList == null)
            {
                errorList = new ArrayList<>();
                errors.put(componentId, errorList);
            }
            errorList.add(error);
        }
    }

    public Map<String, ComponentData> getComponentData()
    {
        return componentData;
    }


    public void registerTranslation(String tag)
    {
        translations.add(tag);
    }


    public Set<String> getTranslations()
    {
        return translations;
    }


    public Map<String, List<QueryError>> getErrors()
    {
        return errors;
    }
}

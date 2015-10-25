package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.i18n.Translator;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewData
{
    private final String viewName;

    private final Map<String, ComponentData> componentData;

    private final Map<String, String> translations;

    private final Translator translator;

    private final RuntimeContext runtimeContext;


    public ViewData(RuntimeContext runtimeContext, String viewName, Translator translator)
    {
        this.runtimeContext = runtimeContext;
        this.viewName = viewName;
        this.componentData = new HashMap<>();
        this.translator = translator;
        translations = new HashMap<>();
    }


    public String getViewName()
    {
        return viewName;
    }


    public void provide(String componentId, Object vars, Object o)
    {
        componentData.put(componentId, new ComponentData(vars, o));
    }


    public Map<String, ComponentData> getData()
    {
        return componentData;
    }


    public void provideTranslation(String code)
    {
        translations.put(code, translator.translate(runtimeContext, code));
    }
}

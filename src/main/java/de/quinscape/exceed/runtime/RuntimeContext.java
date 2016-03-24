package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.i18n.Translator;

import java.util.Locale;
import java.util.Map;

/**
 * Contains information about the current request and the context in which it happens.
 *
 * @see RuntimeContextHolder#get()
 */
public class RuntimeContext
{
    private final String path;

    private final RuntimeApplication runtimeApplication;

    private final Locale locale;

    private final Translator translator;

    private View view;

    private Map<String, Object> variables;


    public RuntimeContext(RuntimeApplication runtimeApplication,
                          String path, Translator translator, Locale locale)
    {
        this.path = path;
        this.runtimeApplication = runtimeApplication;
        this.translator = translator;
        this.locale = locale;
    }



    public String getPath()
    {
        return path;
    }


    public RuntimeApplication getRuntimeApplication()
    {
        return runtimeApplication;
    }


    public Locale getLocale()
    {
        return locale;
    }


    public Translator getTranslator()
    {
        return translator;
    }


    public void setView(View view)
    {
        this.view = view;
    }


    public View getView()
    {
        return view;
    }


    public void setVariables(Map<String, Object> variables)
    {
        this.variables = variables;
    }


    /**
     * Returns the location params which consist of the path variables and the HTTP parameters received for the
     * current mapping.
     *
     * @return
     */
    public Map<String, Object> getLocationParams()
    {
        return variables;
    }
}

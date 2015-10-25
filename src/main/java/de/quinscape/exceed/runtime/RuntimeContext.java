package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.i18n.Translator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class RuntimeContext
{
    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final ModelMap model;

    private final String path;

    private final RuntimeApplication runtimeApplication;

    private Locale locale;

    private Translator translator;

    private View view;


    public RuntimeContext(HttpServletRequest request, HttpServletResponse response, ModelMap model, RuntimeApplication runtimeApplication,
                          String path, Translator translator)
    {
        this.request = request;
        this.response = response;
        this.model = model;
        this.path = path;
        this.runtimeApplication = runtimeApplication;
        this.translator = translator;
        this.locale = request.getLocale();
    }


    public HttpServletRequest getRequest()
    {
        return request;
    }


    public HttpServletResponse getResponse()
    {
        return response;
    }


    public ModelMap getModel()
    {
        return model;
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


    public void setLocale(Locale locale)
    {
        this.locale = locale;
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
}

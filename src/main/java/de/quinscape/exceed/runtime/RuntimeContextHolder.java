package de.quinscape.exceed.runtime;

import de.quinscape.exceed.runtime.application.RuntimeApplication;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RuntimeContextHolder
{
    private final static ThreadLocal<RuntimeContext> runtimeContextThreadLocal = new ThreadLocal<>();

    public static RuntimeContext get()
    {
        return runtimeContextThreadLocal.get();
    }

    public static void register(RuntimeContext runtimeContext)
    {
        runtimeContextThreadLocal.set(runtimeContext);
    }


    public static void clear()
    {
        register(null);
    }
}

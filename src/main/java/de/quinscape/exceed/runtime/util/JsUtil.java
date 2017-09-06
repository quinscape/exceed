package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.domain.DomainObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.util.Map;

public class JsUtil
{
    private final static Logger log = LoggerFactory.getLogger(JsUtil.class);


    public static NashornScriptEngine createEngine()
    {
        log.debug("Create new js engine");

        NashornScriptEngine engine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");

        final SetImmediateFunction setImmediateFunction = new SetImmediateFunction();
        engine.put("setImmediate", setImmediateFunction);

        return engine;
    }

    public static ScriptContext createNewContext(NashornScriptEngine engine)
    {
        ScriptContext scriptContext = new SimpleScriptContext();
        Bindings defaultBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

        Bindings bindings = engine.createBindings();
        bindings.putAll(defaultBindings);
        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        return scriptContext;
    }

    public static void flushImmediate(ScriptContext scriptContext)
    {
        ((SetImmediateFunction) scriptContext.getAttribute("setImmediate")).flush();
    }

    public static String dumpDomainObject(Object o)
    {
        StringBuilder sb = new StringBuilder();

        if (o instanceof Map)
        {
            Map<String,Object> m = (Map) o;
            sb.append("Map");

            for (Map.Entry<String, Object> entry : m.entrySet())
            {
                dump(sb, entry.getKey(), entry.getValue());
            }
        }
        else if (o instanceof DomainObject)
        {
            final DomainObject domainObject = (DomainObject) o;
            sb.append("DomainObject: type = ").append(domainObject.getDomainType()).append("\n");
            for (String name : domainObject.propertyNames())
            {
                dump(sb, name, domainObject.getProperty(name));
            }
        }
        else if (o instanceof JSObject)
        {
            sb.append("JSObject: type = ").append(((JSObject) o).getMember("_type")).append("\n");
            for (String name : ((JSObject) o).keySet())
            {
                dump(sb, name, ((JSObject) o).getMember(name));
            }
        }

        log.info(sb.toString());

        return sb.toString();
    }


    private static void dump(StringBuilder sb, String name, Object member)
    {
        sb.append("    ").append(name).append(" = ").append(member).append(" ( type = ").append(member !=null ? member.getClass() : null).append(")\n");
    }


    public static <T> T get(Object arg)
    {
        if (ScriptObjectMirror.isUndefined(arg))
        {
            return null;
        }
        return (T) arg;
    }
}

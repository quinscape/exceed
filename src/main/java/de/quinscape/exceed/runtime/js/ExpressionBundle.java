package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.runtime.util.Util;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.Map;
import java.util.Set;

/**
 * Contains all expression js functions generated from an application model.
 *
 */
public final class ExpressionBundle
{
    private final static Logger log = LoggerFactory.getLogger(ExpressionBundle.class);


    /**
     * Application version this bundle was created for.
     */
    private final String version;

    /**
     * CompiledScript of the compiled expression sources
     */
    private final CompiledScript compiledScript;

    private final String source;

    public ExpressionBundle(CompiledScript compiledScript, String source, String version)
    {
        this.compiledScript = compiledScript;
        this.source = source;
        this.version = version;
    }

    /**
     * Returns the application version this bundle was created for.
     * @return
     */
    public String getVersion()
    {
        return version;
    }


    /**
     * Returns the CompiledScript of the compiled expression sources
     * @return
     */
    public CompiledScript getCompiledScript()
    {
        return compiledScript;
    }


    public String getSource()
    {
        return source;
    }

    public static ExpressionBundle fromResults(NashornScriptEngine nashorn, String version, ScriptBuffer scriptBuffer)
    {

        StringBuilder sourceBuf = new StringBuilder();
        dumpResults(sourceBuf, scriptBuffer, scriptBuffer.getPushed());
        dumpResults(sourceBuf, scriptBuffer, scriptBuffer.getResults());

        final String source = sourceBuf.toString();

        try
        {
            CompiledScript compiledScript = null;
            compiledScript = nashorn.compile(source);
            return new ExpressionBundle(compiledScript, source, version);
        }
        catch (ScriptException e)
        {
            log.info("SOURCE CODE:\n" + source);

            throw new ExpressionCompilationException("Error compiling application expressions", e);
        }
    }


    public static void dumpResults(
        StringBuilder sourceBuf, ScriptBuffer scriptBuffer, Map<String, String> results
    )
    {
        for (Map.Entry<String, String> entry : results.entrySet())
        {
            final String identifier = entry.getValue();
            final String code = entry.getKey();

            final Set<String> aliases = scriptBuffer.getAliases(identifier);
            sourceBuf.append("var ").append(identifier).append(" = ").append(code).append("    ");
            if (aliases != null)
            {
                sourceBuf.append("/* REUSED AS: ").append(Util.join(aliases, ", ")).append("*/");
            }
            sourceBuf.append("\n\n\n");
        }
    }
}

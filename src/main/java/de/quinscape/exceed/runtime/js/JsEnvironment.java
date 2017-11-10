package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.meta.ApplicationMetaData;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.js.env.ActionFunction;
import de.quinscape.exceed.runtime.js.env.Console;
import de.quinscape.exceed.runtime.js.env.DomainServiceCreateFunction;
import de.quinscape.exceed.runtime.js.env.ExpressionFunction;
import de.quinscape.exceed.runtime.js.env.IsValidTransitionFunction;
import de.quinscape.exceed.runtime.js.env.NoOpFunction;
import de.quinscape.exceed.runtime.js.env.Promise;
import de.quinscape.exceed.runtime.js.env.ScopeFunction;
import de.quinscape.exceed.runtime.js.env.SecurityFunction;
import de.quinscape.exceed.runtime.js.env.TranslationFunction;
import de.quinscape.exceed.runtime.js.env.UUIDFunction;
import de.quinscape.exceed.runtime.js.env.UpdateScopeFunction;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.provider.ApplicationDomainProvider;
import de.quinscape.exceed.runtime.util.JsUtil;
import de.quinscape.exceed.runtime.util.Util;
import de.quinscape.exceed.runtime.view.ViewData;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.math.BigDecimal;

/**
 * Server-side javascript environment handling the server side evaluation of exceed expressions.
 *
 * Attains thread safety by executing all expressions in a script context copy local to the current thread.
 *
 * All script contexts are interchangeable and contain no client state, just prepared application infrastructure and
 * transpiled application expressions.
 */
public class JsEnvironment
{
    private final static Logger log = LoggerFactory.getLogger(JsEnvironment.class);

    private final static ApplicationDomainProvider applicationDomainProvider = new ApplicationDomainProvider();

    public static final String FROM_SERVER_EXPRESSION = "_converter.fromServer";

    public static final String TO_SERVER_EXPRESSION = "_converter.toServer";

    public static final String TO_JS_DECIMAL = "_decimal";
    public static final String RENDER_TO_STRING = "_renderToString";

    private final NashornScriptEngine nashorn;

    private final ThreadLocal<JsContext> jsContextThreadLocal;

    private final ApplicationMetaData metaData;

    private final ExpressionCompiler expressionCompiler;

    private final ApplicationModel applicationModel;

    private volatile ExpressionBundle expressionBundle;

    private final ActionService actionService;

    /**
     * Creates a new JsEnvironment instance.
     *  @param nashorn               nashorn js engine
     * @param applicationModel      application model
     * @param expressionCompiler    expression compiler
     *
     */
    public JsEnvironment(
        ActionService actionService,
        NashornScriptEngine nashorn,
        ApplicationModel applicationModel,
        ExpressionCompiler expressionCompiler
    )
    {
        this.nashorn = nashorn;
        this.applicationModel = applicationModel;
        this.metaData = applicationModel.getMetaData();
        this.expressionCompiler = expressionCompiler;

        ExpressionBundle result;
        synchronized (this)
        {
            result = this.expressionCompiler.compile(applicationModel);
        }
        this.expressionBundle = result;
        this.actionService = actionService;

        jsContextThreadLocal = new ThreadLocal<>();
    }


    private JsContext get(RuntimeContext runtimeContext)
    {
        final JsContext jsContext = jsContextThreadLocal.get();
        if (jsContext != null && jsContext.isFresh(runtimeContext, expressionBundle))
        {
            log.trace("Reuse {}", jsContext);

            return jsContext;
        }

        log.debug("Create new js context");

        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        if (expressionBundle == null || !expressionBundle.getVersion().equals(applicationModel.getVersion()))
        {
            expressionBundle = expressionCompiler.compile(applicationModel);
        }
        
        final JsContext ctx = new JsContext(runtimeContext, expressionBundle, actionService);
        jsContextThreadLocal.set(ctx);
        return ctx;
    }

    public <T> boolean applyRule(RuntimeContext runtimeContext, DomainRule domainRule, T value)
    {
        final ScriptContext scriptContext = get(runtimeContext).scriptContext;
        return (boolean) evaluate(runtimeContext, scriptContext, ExpressionType.RULE, domainRule.getRuleValue().getAstExpression(), value);
    }

    public Promise execute(RuntimeContext runtimeContext, ASTExpression expression)
    {
        final ScriptContext scriptContext = get(runtimeContext).scriptContext;
        final JSObject result = (JSObject) evaluate(runtimeContext, scriptContext, ExpressionType.ACTION, expression);

        if (result == null)
        {
            return new Promise(nashorn, scriptContext, Promise.resolve(runtimeContext, null));
        }
        else
        {
            return new Promise(nashorn, scriptContext, result);
        }

    }

    public Object getValue(RuntimeContext runtimeContext, ASTExpression expression)
    {
        final ScriptContext scriptContext = get(runtimeContext).scriptContext;

        final String compilationResult = getIdentifier(expression);
        final PropertyModel propertyModel = expression.annotation().getPropertyType();
        if (propertyModel == null)
        {
            throw new IllegalStateException("Unknown result type for value expression: " + ExpressionRenderer.render(expression));
        }

        final Object result = evaluate(runtimeContext, scriptContext, ExpressionType.VALUE, expression);

        final PropertyType propertyType = PropertyType.get(runtimeContext, propertyModel);

        return  propertyType.convertFromJs(runtimeContext, result);
    }
    
    private Object evaluate(RuntimeContext runtimeContext, ScriptContext scriptContext, ExpressionType type, ASTExpression expression, Object... args)
    {
        final String identifier = getIdentifier(expression);

//        if (compilationResult.getType() != type)
//        {
//            throw new IllegalStateException("Expected result type " + type +" but found " + compilationResult.getType());
//        }

        final ScriptObjectMirror fn = (ScriptObjectMirror) scriptContext.getAttribute(identifier);
        if (fn == null || !fn.isFunction())
        {
            throw new IllegalStateException("Could not retrieve function '" + identifier + "' from scriptContext");
        }


        final Object result = fn.call(null, args);

        if (ScriptObjectMirror.isUndefined(result))
        {
            return null;
        }

        return result;
    }


    private String getIdentifier(ASTExpression expression)
    {
        final String identifier = expression.annotation().getIdentifier();
        if (identifier == null)
        {
            throw new IllegalStateException("No compilation result prepared for " + ExpressionRenderer.render(expression) + ". Make sure that all server-side executed expressions are prepared by " + DefaultExpressionCompiler.class.getName());
        }
        return identifier;
    }


    /**
     * Contains the nashorn script context and version information.
     */
    private class JsContext
    {
        /**
         * nashorn script context
         */
        public final ScriptContext scriptContext;

        /**
         * Timestamp of the serverjs bundle this context was created for.
         */
        public final long timestamp;

        /**
         * Application model version this context was created for.
         */
        private final String version;

        private final JSObject fromServer;

        private final JSObject toServer;

        private final JSObject toJsDecimal;

        private final JSObject renderToString;


        // XXX: refactor configuration process into a service or something
        public JsContext(RuntimeContext runtimeContext, ExpressionBundle expressionBundle, ActionService actionService)
        {
            log.debug("Create new script context");

            final ApplicationMetaData metaData = applicationModel.getMetaData();

            timestamp = JsEnvironment.this.metaData.getServerJsTimestamp();
            version = runtimeContext.getApplicationModel().getVersion();
            try
            {
                ScriptContext scriptContext = JsUtil.createNewContext(nashorn);

                // get JavaScript "global" object
                JSObject global = (JSObject) nashorn.eval("this", scriptContext);
                // get JS "Object" constructor object
                JSObject jsObject = (JSObject) nashorn.eval("Object", scriptContext);


                //final Bindings global = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
                global.setMember("global", global);
                global.setMember("window", global);

                global.setMember("console", Console.getConsoleAPI());

                global.setMember("__lookup_i18n", new TranslationFunction());
                global.setMember("__Expression", new ExpressionFunction());
                global.setMember("__domainService_create", new DomainServiceCreateFunction(runtimeContext.getDomainService()));


                final CompiledScript serverCommonJsBundle = metaData.getServerCommonJsBundle();
                final CompiledScript serverJsBundle = metaData.getServerJsBundle();
                final CompiledScript serverRenderJsBundle = metaData.getServerRenderJsBundle();

                serverCommonJsBundle.eval(scriptContext);
                serverJsBundle.eval(scriptContext);
                if (serverRenderJsBundle != null)
                {
                    serverRenderJsBundle.eval(scriptContext);
                }
                else
                {
                    global.setMember(RENDER_TO_STRING, NoOpFunction.INSTANCE);
                }

                expressionBundle.getCompiledScript().eval(scriptContext);

                JSObject viewAPI = (JSObject) global.getMember("_v");
                JSObject actionAPI = (JSObject) global.getMember("_a");
                viewAPI.setMember("scope", new ScopeFunction(nashorn, scriptContext, applicationModel));
                viewAPI.setMember("updateScope", new UpdateScopeFunction(nashorn, scriptContext, applicationModel));
                viewAPI.setMember("isValidTransition", new IsValidTransitionFunction(nashorn, scriptContext, applicationModel));
                viewAPI.setMember("uuid", UUIDFunction.INSTANCE);
                viewAPI.setMember("isAdmin", SecurityFunction.IS_ADMIN);
                viewAPI.setMember("hasRole", SecurityFunction.HAS_ROLE);

                actionAPI.setMember("action", new ActionFunction(actionService));

                final ClientData clientData = applicationDomainProvider.provide(null, runtimeContext, new ViewData());
                nashorn.eval("_domainService.init(" + clientData.getJSON() + ")", scriptContext);

                if (log.isDebugEnabled())
                {
                    log.debug("Globals: {}", Util.join(global.keySet(), ", "));
                }

                this.fromServer = getJsFunction(nashorn, scriptContext, FROM_SERVER_EXPRESSION, false);
                this.toServer = getJsFunction(nashorn, scriptContext, TO_SERVER_EXPRESSION, false);
                this.toJsDecimal = getJsFunction(nashorn, scriptContext, TO_JS_DECIMAL, false);
                this.renderToString = getJsFunction(nashorn, scriptContext, RENDER_TO_STRING, true);

                this.scriptContext = scriptContext;

            }
            catch (Exception e)
            {
                throw new ExceedRuntimeException("Error creating script context", e);
            }
        }

        /**
         * Returns <code>false</code> if the js context is no longer fresh due to a change in application model version
         * or server js bundle timestamp.
         *
         * @param runtimeContext        runtime context
         * @param expressionBundle      expression bundle
         * @return  <code>true</code> if still fresh/up to date, <code>false</code> if stale.
         */
        public boolean isFresh(RuntimeContext runtimeContext, ExpressionBundle expressionBundle)
        {
            return metaData.getServerJsTimestamp() <= timestamp &&
                runtimeContext.getApplicationModel().getVersion().equals(version);

        }

        @Override
        public String toString()
        {
            return super.toString() + ": "
                + "timestamp = " + timestamp
                + ", version = '" + version + '\''
                ;
        }
    }


    private static JSObject getJsFunction(
        NashornScriptEngine nashorn,
        ScriptContext scriptContext,
        String expression,
        boolean optional
    )

    {
        try
        {
            final JSObject fn = (JSObject) nashorn.eval(expression, scriptContext);
            if (!optional && (fn == null || !fn.isFunction()))
            {
                throw new IllegalStateException(expression + " is not a function");
            }
            return fn;
        }
        catch (ScriptException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    /**
     * Converts the given java value of the current property type into the nashorn runtime property value of the given property type.
     *
     * @param runtimeContext    runtime context
     * @param value             property value
     * @param propertyModel     property model
     *
     * @return value converted to Js
     */
    public Object convertViaJSONToJs(RuntimeContext runtimeContext, Object value, PropertyModel propertyModel)
    {
        PropertyType propertyType = PropertyType.get(runtimeContext, propertyModel);

        final JsContext jsContext = get(runtimeContext);

        final PropertyConverter converter = propertyType.getConverter();
        final PropertyTypeModel propertyTypeModel = propertyType.getPropertyTypeModel();


        final Object jsonValue = converter.convertToJSON(runtimeContext, value);

        if (log.isDebugEnabled())
        {
            log.debug("Java -> JSON -> Js conversion {} to {} ( type = {}", value, jsonValue, propertyTypeModel);
        }


        return jsContext.fromServer.call(null, jsonValue, DomainProperty.builder()
            .withType(propertyType.getType(), propertyType.getTypeParam())
            .withConfigMap(propertyType.getConfig())
            .build());
    }


    public Object convertViaJSONFromJs(RuntimeContext runtimeContext, Object value, PropertyModel propertyModel)
    {
        PropertyType propertyType = PropertyType.get(runtimeContext, propertyModel);

        final JsContext jsContext = get(runtimeContext);

        final PropertyConverter converter = propertyType.getConverter();

        final DomainProperty returnType = DomainProperty.builder()
            .withType(propertyType.getType(), propertyType.getTypeParam())
            .withConfigMap(propertyType.getConfig())
            .build();

        final Object converted = jsContext.toServer.call(null, value, returnType);

        final Object result = converter.convertToJava(runtimeContext, converted);
        if (log.isDebugEnabled())
        {
            log.debug("Js -> JSON -> Java conversion {} to {} ( type = {}", value, result, propertyType);
        }
        return result;
    }

    public JSObject toDecimal(RuntimeContext runtimeContext, BigDecimal bigDecimal)
    {
        if (bigDecimal == null)
        {
            throw new IllegalArgumentException("bigDecimal can't be null");
        }

        return (JSObject) get(runtimeContext).toJsDecimal.call(null, bigDecimal.toString());
    }

    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public NashornScriptEngine getNashorn()
    {
        return nashorn;
    }

    public ScriptContext getScriptContext(RuntimeContext runtimeContext)
    {
        return get(runtimeContext).scriptContext;
    }


    /**
     * Uses server-side react rendering to render a markup string from the given view-data JSON blob.
     *
     * @param runtimeContext    runtime context
     * @param viewDataJson      view data JSON
     * @return prerendered react markup as string
     */
    public String renderToString(RuntimeContext runtimeContext, String viewDataJson)
    {
        final JSObject renderToString = get(runtimeContext).renderToString;
        if (renderToString == null)
        {
            return "<!-- server-side rendering disabled -->";
        }
        return (String) renderToString.call(null, viewDataJson);
    }
}

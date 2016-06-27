package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.config.DefaultPropertyConverters;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.DomainServiceRepository;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.svenson.JSON;
import org.svenson.TypeAnalyzer;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectSupport;
import org.svenson.util.JSONBeanUtil;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller handling the server-side of the unified action system.
 */
@Controller
public class ActionController
{
    private final static Logger log = LoggerFactory.getLogger(ActionController.class);

    private static final String RESULT_OK = "{\"ok\":true}";


    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ActionService actionService;

    @Autowired
    private RuntimeContextFactory runtimeContextFactory;

    @Autowired
    private DefaultPropertyConverters defaultPropertyConverters;

    @Autowired
    private ScopedContextFactory scopedContextFactory;

    @Autowired
    private DomainServiceRepository domainServiceRepository;

    private final JSON resultGenerator;

    private Map<String, Class<? extends ActionModel>> modelsByName;

    private Map<Class<? extends ActionModel>, Set<Conversion>> conversionLookup;


    public ActionController()
    {
        resultGenerator = JSON.defaultJSON();
    }


    @RequestMapping(value = "/action/{app}/{action}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> executeAction(
        @PathVariable("app") String appName,
        @PathVariable("action") String actionName,
        @RequestBody String actionModelJSON,
        HttpServletRequest request, HttpServletResponse response) throws IOException
    {

        // TODO: action security?
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            return new ResponseEntity<String>("{\"ok\":false,\"error\":\"Action not found\"}", HttpStatus.NOT_FOUND);
        }

        Class<? extends ActionModel> actionClass = modelsByName.get(actionName);
        if (actionClass == null)
        {
            throw new ActionNotFoundException("Action '" + actionName + "' not found.");
        }

        SessionContext sessionContext = scopedContextFactory.getSessionContext(request, appName, runtimeApplication.getApplicationModel().getSessionContext());
        RuntimeContext runtimeContext = runtimeContextFactory.create(
            runtimeApplication, "/action/" + appName + "/" + actionName, request.getLocale(),
            new ScopedContextChain(
                Arrays.asList(
                    runtimeApplication.getApplicationContext(),
                    sessionContext
                )
            ), domainServiceRepository.getDomainService(appName));

        RuntimeContextHolder.register(runtimeContext);
        scopedContextFactory.initializeContext(runtimeContext, sessionContext);

        RuntimeContextHolder.register(runtimeContext);

        ActionModel model = (ActionModel) runtimeContext.getDomainService().toDomainObject(actionClass, actionModelJSON);
        convertProperties(runtimeContext, model);

        response.setContentType(ContentType.JSON);

        Action action = actionService.getAction(model.getAction());
        try
        {
            action.execute(runtimeContext, model);
            return new ResponseEntity<String>(RESULT_OK, HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Error executing action " + action + ", model = " + actionModelJSON, e);

            String errorJSON = "{\"ok\":false,\"error\":" + resultGenerator.quote("Action " +
                actionModelJSON + " failed") + "}";

            return new ResponseEntity<String>(errorJSON, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        finally
        {
            RuntimeContextHolder.clear();
        }
    }


    private void convertProperties(RuntimeContext runtimeContext, ActionModel model)
    {
        try
        {
            Set<Conversion> conversions = conversionLookup.get(model.getClass());
            if (conversions != null && conversions.size() > 0)
            {
                for (Conversion conversion : conversions)
                {
                    conversion.convert(runtimeContext, model);
                }
            }
        }
        catch (ParseException e)
        {
            throw new ActionExecutionException("Error converting domain object properties", e);
        }
    }


    @ExceptionHandler(value = ActionNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> actionNotFound(HttpServletResponse response, ActionNotFoundException e)
    {
        response.setContentType(ContentType.JSON);
        return new ResponseEntity<String>("{\"ok\":false,\"error\":" + resultGenerator.quote(e.getMessage()) + "}",
            HttpStatus.NOT_FOUND);
    }


    @PostConstruct
    public void init()
    {
        modelsByName = actionService.getActionModels();

        Map<Class<? extends ActionModel>, Set<Conversion>> map = new HashMap<>();

        for (Class<? extends ActionModel> modelClass : modelsByName.values())
        {
            map.put(modelClass, analyze(modelClass));
        }

        conversionLookup = map;

        log.info("CONVERSIONS: {}", conversionLookup);
    }


    private JavaObjectSupport objectSupport = new JavaObjectSupport();


    private Set<Conversion> analyze(Class<? extends ActionModel> modelClass)
    {
        Set<Conversion> conversions = new HashSet<>();

        JSONClassInfo info = TypeAnalyzer.getClassInfo(objectSupport, modelClass);
        for (JSONPropertyInfo propertyInfo : info.getPropertyInfos())
        {
            if (DomainObject.class.isAssignableFrom(propertyInfo.getType()))
            {
                conversions.add(new PropertyConversion(propertyInfo.getJsonName()));
            }
            else if (Collection.class.isAssignableFrom(propertyInfo.getType()))
            {
                Class<Object> typeHint = propertyInfo.getTypeHint();
                if (typeHint != null && DomainObject.class.isAssignableFrom(typeHint))
                {
                    conversions.add(new CollectionConversion(propertyInfo.getType(), propertyInfo.getJsonName()));
                }
            }
        }

        return conversions;
    }


    private abstract class Conversion
    {
        abstract String getName();

        @Override
        public String toString()
        {
            return super.toString() + ": " + getName();
        }


        public abstract void convert(RuntimeContext runtimeContext, ActionModel model) throws ParseException;
    }


    private class PropertyConversion
        extends Conversion
    {
        private final String name;


        public PropertyConversion(String name)
        {
            this.name = name;
        }


        public String getName()
        {
            return name;
        }


        @Override
        public void convert(RuntimeContext runtimeContext, ActionModel model) throws ParseException
        {
            DomainObject value = (DomainObject) JSONBeanUtil.defaultUtil().getProperty(model, name);
            if (value == null)
            {
                return;
            }
            value = DomainUtil.convertToJava(runtimeContext, value);
        }
    }

    private class CollectionConversion
        extends Conversion
    {

        private final String name;

        private final boolean isList;


        public CollectionConversion(Class<Object> collectionType, String name)
        {
            this.isList = List.class.isAssignableFrom(collectionType);
            if (!this.isList && !Set.class.isAssignableFrom(collectionType))
            {
                throw new IllegalStateException("Collection type must be list or set");
            }
            this.name = name;
        }

        public String getName()
        {
            return name;
        }


        @Override
        public void convert(RuntimeContext runtimeContext, ActionModel model) throws ParseException
        {
            Object value = JSONBeanUtil.defaultUtil().getProperty(model, name);
            if (value == null)
            {
                return;
            }

            if (isList)
            {
                List<DomainObject> domainObjects = new ArrayList<>();

                for (DomainObject domainObject : (List<DomainObject>) value)
                {
                    domainObject = DomainUtil.convertToJava(runtimeContext, domainObject);
                }
            }
        }
    }




    private PropertyConverter getConverter(String type)
    {
        Map<String, PropertyConverter> converters = defaultPropertyConverters.getConverters();
        String converterName = type + "Converter";
        PropertyConverter converter = converters.get(converterName);
        if (converter == null)
        {
            throw new IllegalStateException("No property converter '" + converterName + "' found for type '" + type +"'" );
        }
        return converter;
    }
}

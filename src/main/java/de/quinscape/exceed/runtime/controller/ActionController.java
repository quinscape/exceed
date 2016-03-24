package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.util.ContentType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Controller handling the server-side of the unified action system.
 */
@Controller
public class ActionController
{
    private static Logger log = LoggerFactory.getLogger(ActionController.class);


    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ActionRegistry actionRegistry;

    @Autowired
    private RuntimeContextFactory runtimeContextFactory;

    private final JSON resultGenerator;

    private final JSONParser dataParser;

    private final JSONParser actionModelParser;


    public ActionController()
    {
        resultGenerator = JSON.defaultJSON();
        dataParser = JSONParser.defaultJSONParser();

        actionModelParser = new JSONParser();
    }


    @RequestMapping(value = "/action/{app}/{action}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> executeAction(
        @PathVariable("app") String appName,
        @PathVariable("action") String actionName,
        @RequestParam("model") String actionModelJSON,
        @RequestBody String dataJSON,
        HttpServletRequest request, HttpServletResponse response) throws IOException
    {

        // TODO: action security?
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            return new ResponseEntity<String>("{\"ok\":false,\"error\":\"Action not found\"}", HttpStatus.NOT_FOUND);
        }

        ActionModel model = actionModelParser.parse(ActionModel.class, actionModelJSON);

        response.setContentType(ContentType.JSON);

        if (!model.getAction().equals(actionName))
        {
            return new ResponseEntity<String>("{\"ok\":false,\"error\":\"Action name mismatch\"}", HttpStatus
                .BAD_REQUEST);
        }

        Action action = actionRegistry.getAction(model.getAction());

        RuntimeContext runtimeContext = runtimeContextFactory.create(
            runtimeApplication, "/action/" + appName + "/" + actionName, request.getLocale());
        RuntimeContextHolder.register(runtimeContext);

        try
        {
            Object input = parseData(runtimeContext, action.getInputClass(),dataJSON);
            Object result = action.execute(runtimeContext, model, input);
            String resultJSON;

            if (result instanceof DataList)
            {
                resultJSON = runtimeApplication.getDomainService().toJSON(result);
            }
            else
            {
                resultJSON = resultGenerator.forValue(result);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Executed action {}: {} => {}", actionModelJSON, dataJSON, resultJSON);
            }

            return new ResponseEntity<String>(resultJSON, HttpStatus.OK);
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


    @ExceptionHandler(value = ActionNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> actionNotFound(HttpServletResponse response, ActionNotFoundException e)
    {
        response.setContentType(ContentType.JSON);
        return new ResponseEntity<String>("{\"ok\":false,\"error\":" + resultGenerator.quote(e.getMessage()) + "}",
            HttpStatus.NOT_FOUND);
    }


    private Object parseData(RuntimeContext runtimeContext, Class inputClass, String dataJSON)
    {
        if (DataList.class.isAssignableFrom(inputClass))
        {
            return runtimeContext.getRuntimeApplication().getDomainService().toDomainObject(dataJSON);
        }
        return dataParser.parse(inputClass, dataJSON);
    }


//    @RequestMapping(value = "/action/{app}", method = RequestMethod.GET)
//    @ResponseBody
//    public String listActions(
//        @PathVariable("app") String appName,
//        HttpServletRequest request, HttpServletResponse response) throws IOException
//    {
//        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
//        if (runtimeApplication == null)
//        {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
//            return null;
//        }
//
//        String json;
//
//        Map<String, Object> data = new HashMap<>();
//
//        data.put("actionNames", actionRegistry.getActionNames());
//
//        response.setContentType(ContentType.JSON);
//        return resultGenerator.forValue(data);
//    }

    @PostConstruct
    public void init()
    {
        actionModelParser.setTypeMapper(new ActionMapper(actionRegistry.getActionModels()));
    }


    private class ActionMapper
        extends AbstractPropertyValueBasedTypeMapper
    {
        private final Map<String, Class<? extends ActionModel>> actionModelClassess;


        public ActionMapper(Map<String, Class<? extends ActionModel>> actionModelClassess)
        {
            this.actionModelClassess = actionModelClassess;

            setDiscriminatorField("action");
            setPathMatcher(new SubtypeMatcher(ActionModel.class));
        }


        @Override
        protected Class<? extends ActionModel> getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            if (o instanceof String)
            {
                Class<? extends ActionModel> actionModelClass = actionModelClassess.get(o);
                if (actionModelClass == null)
                {
                    throw new ActionNotFoundException("Invalid action reference in JSON: " + o);
                }
                return actionModelClass;
            }

            throw new ActionNotFoundException("Invalid action discriminator value: " + o);
        }
    }

}

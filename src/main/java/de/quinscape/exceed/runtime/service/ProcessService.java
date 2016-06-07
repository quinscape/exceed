package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.process.DecisionModel;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.process.ProcessExecution;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.process.TransitionData;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ProcessService
{
    private final static Logger log = LoggerFactory.getLogger(ProcessService.class);

    private final ActionService actionService;

    private final ExpressionService expressionService;

    private final ScopedContextFactory scopedContextFactory;


    public ProcessService(ActionService actionService, ExpressionService expressionService, ScopedContextFactory
        scopedContextFactory)
    {
        this.actionService = actionService;
        this.expressionService = expressionService;
        this.scopedContextFactory = scopedContextFactory;
    }


    public ProcessExecutionState start(RuntimeContext runtimeContext, Process process)
    {
        String appName = runtimeContext.getApplicationModel().getName();

        ContextModel contextModel = process.getContext();

        ProcessContext scopedContext = scopedContextFactory.createProcessContext(contextModel);
        scopedContextFactory.initializeContext(runtimeContext, scopedContext);

        ProcessExecution execution = new ProcessExecution(appName, process.getName());

        return transition(runtimeContext, execution, scopedContext, process.getStartTransition(), null);
    }


    public ProcessExecutionState resume(RuntimeContext runtimeContext, ProcessExecutionState state, String
        transition, String contextJSON)
    {
        if (transition == null)
        {
            throw new IllegalArgumentException("transition can't be null");
        }


        DomainObject partialDomainObjectContext = null;
        if (StringUtils.hasText(contextJSON))
        {
            try
            {
                partialDomainObjectContext = getTransitionDomainContext(runtimeContext, contextJSON);
            }
            catch (ParseException e)
            {
                throw new ProcessExecutionException("Error converting context JSON for state '" + state + "':", e);
            }
        }


        ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        ProcessExecution execution = state.getExecution();

        Process process = applicationModel.getProcess(execution.getProcessName());

        ProcessState processState = process.getStates().get(state.getCurrentState());

        if (!(processState instanceof ViewState))
        {
            throw new ProcessExecutionException("Current state is not a view state");
        }

        Transition transitionModel = ((ViewState) processState).getTransitions().get(transition);

        if (transitionModel == null)
        {
            throw new ProcessExecutionException("View state '" + processState.getName() + "' has no transition '" +
                transition + "'");
        }

        return transition(runtimeContext, state.getExecution(), state.getScopedContext(), transitionModel,
            partialDomainObjectContext);
    }


    private ProcessExecutionState transition(RuntimeContext runtimeContext, ProcessExecution execution,
                                             ProcessContext scopedContext, Transition transitionModel, DomainObject
                                                 partialDomainObjectContext)
    {
        ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        ProcessContext newScopedCtx = (ProcessContext) scopedContext.copy(runtimeContext);


        DomainObject domainObjectContext;
        if (transitionModel.isMergeContext())
        {
            DomainService domainService = runtimeContext.getDomainService();
            DomainObject fullObject = partialDomainObjectContext != null ? domainService.read
                (partialDomainObjectContext.getDomainType(), partialDomainObjectContext.getId()) : null;
            if (fullObject != null)
            {
                domainObjectContext = mergeInto(fullObject, partialDomainObjectContext);
            }
            else
            {
                domainObjectContext = partialDomainObjectContext;
            }
        }
        else
        {
            domainObjectContext = partialDomainObjectContext;
        }

        newScopedCtx.setDomainObjectContext(domainObjectContext);

        runtimeContext.getScopedContextChain().addContext(newScopedCtx);

        ASTExpression actionAST = transitionModel.getActionAST();
        if (actionAST != null)
        {
            ActionExecutionEnvironment environment = new ActionExecutionEnvironment(runtimeContext, runtimeContext
                .getScopedContextChain(), actionService);
            expressionService.evaluate(actionAST, environment);
        }

        String newStateName = transitionModel.getTo();

        Process process = applicationModel.getProcess(execution.getProcessName());

        ProcessState targetStateModel = process.getStates().get(newStateName);

        // internally handled states
        if (targetStateModel instanceof DecisionState)
        {
            return decide(runtimeContext, execution, newScopedCtx, (DecisionState) targetStateModel,
                domainObjectContext);
        }

        // user handled states
        return new ProcessExecutionState(execution, newScopedCtx, newStateName);
    }


    private DomainObject mergeInto(DomainObject fullObject, DomainObject partialDomainObjectContext)
    {
        for (String name : partialDomainObjectContext.propertyNames())
        {
            if (!name.equals("_type"))
            {
                Object value = partialDomainObjectContext.getProperty(name);
                if (value != null)
                {
                    fullObject.setProperty(name, value);
                }
            }
        }
        return fullObject;
    }


    private ProcessExecutionState decide(RuntimeContext runtimeContext, ProcessExecution execution, ProcessContext
        newScopedCtx, DecisionState decisionStateModel, DomainObject domainObjectContext)
    {

        for (DecisionModel decisionModel : decisionStateModel.getDecisions())
        {
            ActionExecutionEnvironment environment = new ActionExecutionEnvironment(runtimeContext, runtimeContext
                .getScopedContextChain(), actionService);
            Object result = expressionService.evaluate(decisionModel.getExpressionAST(), environment);

            if (!(result instanceof Boolean))
            {
                throw new ProcessExecutionException("Decision expression '" + decisionModel.getExpression() + "' did not " +
                    "return a boolean result but " + result);
            }

            if ((Boolean) result)
            {
                return transition(runtimeContext, execution, newScopedCtx, decisionModel.getTransition(),
                    domainObjectContext);
            }
        }

        return transition(runtimeContext, execution, newScopedCtx, decisionStateModel.getDefaultTransition(),
            domainObjectContext);
    }


    private DomainObject getTransitionDomainContext(RuntimeContext runtimeContext, String json) throws ParseException
    {
        DomainObject partialDomainObjectContext;
        log.debug("Domain Object context: {}", json);

        TransitionData transitionData = runtimeContext.getDomainService().toDomainObject(TransitionData.class, json);
        partialDomainObjectContext = transitionData.getObjectContext();

        if (partialDomainObjectContext != null && partialDomainObjectContext.getDomainType() == null)
        {
            partialDomainObjectContext = null;
        }
        partialDomainObjectContext = DomainUtil.convertToJava(runtimeContext, partialDomainObjectContext);

        log.debug("Partial object context: {}", partialDomainObjectContext);
        return partialDomainObjectContext;
    }
}

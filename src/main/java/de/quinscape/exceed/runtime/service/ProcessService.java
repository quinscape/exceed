package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.process.DecisionModel;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.process.ProcessExecution;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.process.TransitionData;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopeType;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.ViewContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.debug("Start {}", process.getName());

        String appName = runtimeContext.getApplicationModel().getName();

        ContextModel contextModel = process.getContextModel();

        ProcessContext scopedContext = scopedContextFactory.createProcessContext(contextModel);
        scopedContextFactory.initializeContext(runtimeContext, scopedContext);

        ProcessExecution execution = new ProcessExecution(appName, process.getName());

        return transition(runtimeContext, execution, scopedContext, process.getStartTransition(), new TransitionData());
    }


    public ProcessExecutionState resume(RuntimeContext runtimeContext, ProcessExecutionState state, String
        transition, TransitionData transitionData)
    {
        if (transition == null)
        {
            throw new IllegalArgumentException("transition can't be null");
        }


        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        final ProcessExecution execution = state.getExecution();

        final Process process = applicationModel.getProcess(execution.getProcessName());

        final String currentState = state.getCurrentState();
        final ProcessState processState = process.getStates().get(currentState);

        if (!(processState instanceof ViewState))
        {
            throw new ProcessExecutionException("Current state '" + processState.getName() + "' is not a view state");
        }

        Transition transitionModel = ((ViewState) processState).getTransitions().get(transition);

        if (transitionModel == null)
        {
            throw new ProcessExecutionException("View state '" + processState.getName() + "' has no transition '" +
                transition + "'");
        }

        return transition(runtimeContext, state.getExecution(), state.getScopedContext(), transitionModel,
            transitionData);
    }


    private ProcessExecutionState transition(RuntimeContext runtimeContext, ProcessExecution execution,
                                             ProcessContext scopedContext, Transition transitionModel, TransitionData transitionData)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        final String processName = execution.getProcessName();
        final Process process = applicationModel.getProcess(processName);
        final ProcessState sourceState = process.getStates().get(transitionModel.getFrom());

        if (log.isDebugEnabled())
        {
            log.debug(
                "Process {}: Transition '{}' from {} to {} ({})",
                processName,
                transitionModel.getName(),
                sourceState != null ? sourceState.getName() : "start",
                transitionModel.getTo(),
                transitionData
            );
        }

        final ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

        ProcessContext newScopedCtx = (ProcessContext) scopedContext.copy(runtimeContext);
        newScopedCtx.update(transitionData.getContextUpdate());

        DomainObject domainObjectContext;
        DomainObject partialDomainObjectContext = transitionData.getObjectContext();
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



        scopedContextChain.update(newScopedCtx, sourceState == null ? null : Process.getProcessViewName(process.getName(), sourceState.getName()));

        ViewContext viewContext = null;
        if (sourceState instanceof ViewState)
        {
            final View view = process.getView(sourceState.getName());
            viewContext = scopedContextFactory.createViewContext(view);
            scopedContextFactory.initializeContext(runtimeContext, viewContext);

            if (sourceState.getName().equals(transitionData.getStateName()))
            {
                viewContext.update(transitionData.getContextUpdate());
            }

            scopedContextChain.update(viewContext, view.getName());
        }

        ASTExpression actionAST = transitionModel.getActionAST();
        if (actionAST != null)
        {
            ActionExecutionEnvironment environment = new ActionExecutionEnvironment(runtimeContext, scopedContextChain, actionService, processName);
            expressionService.evaluate(actionAST, environment);
            transitionData.setContextUpdate(viewContext != null ? viewContext.getContext() : null);
        }

        String newStateName = transitionModel.getTo();

        ProcessState targetStateModel = process.getStates().get(newStateName);

        // internally handled states
        if (targetStateModel instanceof DecisionState)
        {
            scopedContextChain.clearContext(ScopeType.VIEW);
            return decide(runtimeContext, execution, newScopedCtx, (DecisionState) targetStateModel, transitionData);
        }
        else if (targetStateModel instanceof ViewState)
        {
            View view = process.getView(targetStateModel.getName());
            if (!view.getName().equals(scopedContextChain.getScopeLocation()))
            {
                // entered a new view, still have the old view context.
                // We recreate the view context for the new view.
                final ViewContext newViewContext = scopedContextFactory.createViewContext(view);
                scopedContextFactory.initializeContext(runtimeContext, newViewContext);
                runtimeContext.getScopedContextChain().update(newViewContext, view.getName());
            }
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
        newScopedCtx, DecisionState decisionStateModel, TransitionData transitionData)
    {

        for (DecisionModel decisionModel : decisionStateModel.getDecisions())
        {
            ActionExecutionEnvironment environment = new ActionExecutionEnvironment(runtimeContext, runtimeContext
                .getScopedContextChain(), actionService, execution.getProcessName());
            Object result = expressionService.evaluate(decisionModel.getExpressionAST(), environment);

            if (!(result instanceof Boolean))
            {
                throw new ProcessExecutionException("Decision expression '" + decisionModel.getExpression() + "' did not " +
                    "return a boolean result but " + result);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Decision '{}' is {}", decisionModel.getExpression(), result);
            }

            if ((Boolean) result)
            {
                return transition(runtimeContext, execution, newScopedCtx, decisionModel.getTransition(), transitionData);
            }
        }

        return transition(runtimeContext, execution, newScopedCtx, decisionStateModel.getDefaultTransition(), transitionData);
    }
}

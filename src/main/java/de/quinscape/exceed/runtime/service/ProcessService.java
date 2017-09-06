package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.process.DecisionModel;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.js.env.Promise;
import de.quinscape.exceed.runtime.js.env.PromiseState;
import de.quinscape.exceed.runtime.process.ProcessExecution;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.process.TransitionInput;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopeType;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.ViewContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProcessService
{
    private final static Logger log = LoggerFactory.getLogger(ProcessService.class);

    private final ScopedContextFactory scopedContextFactory;


    public ProcessService(ScopedContextFactory scopedContextFactory)
    {
        this.scopedContextFactory = scopedContextFactory;
    }


    public ProcessExecutionState start(RuntimeContext runtimeContext, Process process)
    {
        log.debug("Start {}", process.getName());

        String appName = runtimeContext.getApplicationModel().getName();

        ContextModel contextModel = process.getContextModel();

        ProcessContext scopedContext = scopedContextFactory.createProcessContext(contextModel);
        scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, scopedContext);

        ProcessExecution execution = new ProcessExecution(appName, process.getName());

        return transition(runtimeContext, execution, scopedContext, process.getStartTransition(), new TransitionInput());
    }


    public ProcessExecutionState resume(
        RuntimeContext runtimeContext,
        ProcessExecutionState state,
        String transition,
        TransitionInput transitionInput
    )
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

        return transition(
            runtimeContext,
            state.getExecution(),
            state.getScopedContext(),
            transitionModel,
            transitionInput
        );
    }


    private ProcessExecutionState transition(RuntimeContext runtimeContext, ProcessExecution execution,
                                             ProcessContext scopedContext, Transition transitionModel, TransitionInput transitionInput)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        final String processName = execution.getProcessName();
        final Process process = applicationModel.getProcess(processName);
        final ProcessState sourceState = process.getStates().get(transitionModel.getFrom());
        final String newStateName = transitionModel.getTo();
        final ProcessState targetStateModel = process.getStates().get(newStateName);

        if (log.isDebugEnabled())
        {
            log.debug(
                "Process {}: Transition '{}' from {} to {} ({})",
                processName,
                transitionModel.getName(),
                sourceState != null ? sourceState.getName() : "start",
                transitionModel.getTo(),
                transitionInput
            );
        }

        final ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

        ProcessContext newScopedCtx = (ProcessContext) scopedContext.copy(runtimeContext);

        if (targetStateModel instanceof ViewState)
        {
            final View view = process.getView(targetStateModel.getName());
            final String viewDomainType = view.getDomainType();
            if (viewDomainType != null)
            {
                DomainObject found = null;

                final Map<String, DomainObject> context = transitionInput.getContext();
                if (context != null)
                {
                    for (DomainObject domainObject : context.values())
                    {
                        if (domainObject.getDomainType().equals(viewDomainType))
                        {
                            if (found != null)
                            {
                                // ambiguous domain objects, take none
                                throw new TransitionContextException("More than one object of type '" + viewDomainType + "', cannot choose current");
                            }
                            found = domainObject;
                        }
                    }
                }

                if (found != null)
                {
                    newScopedCtx.setCurrentDomainObject(found);
                }
            }
        }


        final String scopeLocation;

        // if we have no source state, we're in the start transition
        if (sourceState == null)
        {
            // and we use the process scope location for it
            scopeLocation = process.getScopeLocation();
        }
        else
        {
            scopeLocation = Process.getProcessStateName(
                process.getName(),
                sourceState.getName()
            );
        }
        scopedContextChain.update(newScopedCtx, scopeLocation);

        scopedContextChain.update(transitionInput.getContextUpdate());

        ViewContext viewContext = null;
        final JsEnvironment jsEnvironment = runtimeContext.getJsEnvironment();
        if (sourceState instanceof ViewState)
        {
            final View view = process.getView(sourceState.getName());
            viewContext = scopedContextFactory.createViewContext(view);
            scopedContextFactory.initializeContext(jsEnvironment, runtimeContext, viewContext);
            scopedContextChain.update(viewContext, view.getName());

            if (sourceState.getName().equals(transitionInput.getStateName()))
            {
                scopedContextChain.update(transitionInput.getContextUpdate());
            }

        }

        ASTExpression actionAST = transitionModel.getActionAST();
        if (actionAST != null)
        {
            final Promise promise = jsEnvironment.execute(runtimeContext, actionAST);
            if (promise.getState() == PromiseState.REJECTED && !Boolean.FALSE.equals(promise.getResult()))
            {
                final StringBuilder buff = new StringBuilder();
                buff.append("Error executing transition '")
                    .append(transitionModel.getName())
                    .append("'");
                if (sourceState != null)
                {
                    buff.append(" from '")
                        .append(sourceState.getScopeLocation())
                        .append("'");
                }
                buff.append(": action = ")
                    .append(transitionModel.getAction());

                final Object result = promise.getResult();
                if (result instanceof Throwable)
                {
                    throw new ProcessExecutionException(buff.toString(), (Throwable)result);
                }
                else
                {
                    buff.append(result);

                    throw new ProcessExecutionException(buff.toString());
                }
            }
            transitionInput.setContextUpdate(viewContext != null ? viewContext.getContext() : null);
        }


        // internally handled states
        if (targetStateModel instanceof DecisionState)
        {
            scopedContextChain.clearContext(ScopeType.VIEW);
            return decide(runtimeContext, execution, newScopedCtx, (DecisionState) targetStateModel, transitionInput);
        }
        else if (targetStateModel instanceof ViewState)
        {
            View view = process.getView(targetStateModel.getName());
            if (!view.getName().equals(scopedContextChain.getScopeLocation()))
            {
                // entered a new view, still have the old view context.
                // We recreate the view context for the new view.
                final ViewContext newViewContext = scopedContextFactory.createViewContext(view);
                scopedContextFactory.initializeContext(jsEnvironment, runtimeContext, newViewContext);
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
            if (!name.equals(DomainType.TYPE_PROPERTY))
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
        newScopedCtx, DecisionState decisionStateModel, TransitionInput transitionInput)
    {
        final JsEnvironment jsEnvironment = runtimeContext.getJsEnvironment();

        for (DecisionModel decisionModel : decisionStateModel.getDecisions())
        {
            Object result = jsEnvironment.getValue(runtimeContext, decisionModel.getExpressionAST());

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
                return transition(runtimeContext, execution, newScopedCtx, decisionModel.getTransition(), transitionInput);
            }
        }

        return transition(runtimeContext, execution, newScopedCtx, decisionStateModel.getDefaultTransition(), transitionInput);
    }
}

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
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.process.ProcessExecution;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessService
{
    @Autowired
    private ActionService actionService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private ScopedContextFactory scopedContextFactory;

    public ProcessExecutionState start(RuntimeContext runtimeContext, Process process)
    {
        String appName = runtimeContext.getRuntimeApplication().getApplicationModel().getName();

        ContextModel contextModel = process.getContext();

        ScopedContext scopedContext = scopedContextFactory.createProcessContext(contextModel);
        scopedContextFactory.initializeContext(runtimeContext, scopedContext);

        ProcessExecution execution = new ProcessExecution(appName, process.getName());

        return transition(runtimeContext, execution, scopedContext, process.getStartTransition());
    }

    public ProcessExecutionState resume(RuntimeContext runtimeContext, ProcessExecutionState state, String transition)
    {
        if (transition == null)
        {
            throw new IllegalArgumentException("transition can't be null");
        }

        ApplicationModel applicationModel = runtimeContext.getRuntimeApplication().getApplicationModel();

        ProcessExecution execution = state.getExecution();

        Process process = applicationModel.getProcess(execution.getProcessName());

        ProcessState processState = process.getStates().get(state.getCurrentState());

        if (!(processState instanceof ViewState))
        {
            throw new IllegalStateException("Current state is not a view state");
        }

        Transition transitionModel = ((ViewState) processState).getTransitions().get(transition);

        if (transitionModel == null)
        {
            throw new IllegalArgumentException("View state '" + processState.getName() + "' has no transition '" + transition + "'");
        }

        return transition(runtimeContext, state.getExecution(), state.getScopedContext(), transitionModel);
    }


    private ProcessExecutionState transition(RuntimeContext runtimeContext, ProcessExecution execution, ScopedContext scopedContext, Transition transitionModel)
    {
        ApplicationModel applicationModel = runtimeContext.getRuntimeApplication().getApplicationModel();

        ScopedContext newScopedCtx = scopedContext.copy(runtimeContext);

        runtimeContext.getScopedContextChain().addContext(newScopedCtx);

        ASTExpression actionAST = transitionModel.getActionAST();
        if (actionAST != null)
        {
            ActionExecutionEnvironment environment = new ActionExecutionEnvironment(runtimeContext, runtimeContext.getScopedContextChain(), actionService);
            expressionService.evaluate(actionAST, environment);
        }

        String newStateName = transitionModel.getTo();

        Process process = applicationModel.getProcess(execution.getProcessName());

        ProcessState targetStateModel = process.getStates().get(newStateName);

        // internally handled states
        if (targetStateModel instanceof DecisionState)
        {
            return decide(runtimeContext, execution, newScopedCtx, (DecisionState) targetStateModel);
        }

        // user handled states
        return new ProcessExecutionState(execution, newScopedCtx, newStateName);
    }


    private ProcessExecutionState decide(RuntimeContext runtimeContext, ProcessExecution execution, ScopedContext newScopedCtx, DecisionState decisionStateModel)
    {

        for (DecisionModel decisionModel : decisionStateModel.getDecisions())
        {
            ActionExecutionEnvironment environment = new ActionExecutionEnvironment(runtimeContext, runtimeContext.getScopedContextChain(), actionService);
            Object result = expressionService.evaluate(decisionModel.getExpressionAST(), environment);

            if (!(result instanceof Boolean))
            {
                throw new IllegalStateException("Decision expression '" + decisionModel.getExpression() + "' did not return a boolean result but " + result);
            }

            if ((Boolean)result)
            {
                return transition(runtimeContext, execution, newScopedCtx, decisionModel.getTransition());
            }
        }

        return transition(runtimeContext, execution, newScopedCtx, decisionStateModel.getDefaultTransition());
    }
}
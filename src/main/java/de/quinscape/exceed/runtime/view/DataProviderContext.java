package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.service.ComponentRegistration;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the necessary context for a view data provisioning invocation for
 * a view at runtime.
 */
public final class DataProviderContext
    implements Cloneable
{
    private final ViewDataService viewDataService;

    private final RuntimeContext runtimeContext;

    private final String viewName;

    private final ProcessExecutionState state;

    private final ViewData viewData;

    private final ExpressionService expressionService;

    private boolean continueOnChildren = true;

    private final ComponentModel overridden;

    private final Map<String, Object> varsOverride;

    public DataProviderContext(ViewDataService viewDataService, RuntimeContext runtimeContext, ExpressionService expressionService, String viewName, ProcessExecutionState state, ViewData viewData, ComponentModel overridden, Map<String, Object> varsOverride)
    {
        this.viewDataService = viewDataService;
        this.viewName = viewName;
        this.runtimeContext = runtimeContext;
        this.expressionService = expressionService;
        this.state = state;
        this.viewData = viewData;
        this.overridden = overridden;
        this.varsOverride = varsOverride;
    }

    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public String getViewName()
    {
        return viewName;
    }

    /**
     * Selectively invokes data providers for the given element node and its children and grand children.
     *
     * <p>
     *     Calling this method once will automatically cause the view data service to skip all children this method
     *     was not called for.
     * </p>
     *
     * @param element    element node
     */
    public void provideFor(ComponentModel element)
    {
        // we continue with a clone of ourselves to make sure that "continue on children" detection can happen
        // independently from this context
        viewDataService.prepareRecursive(this.clone(), element);

        // for this context, we now don't continue on children
        continueOnChildren = false;
    }

    protected DataProviderContext clone()
    {
        try
        {
            DataProviderContext clone = (DataProviderContext) super.clone();

            /**
             * we don't need to clone any of our members.
             * They're all either infrastructure or immutable.
             * Cloning {@link #componentData} would defeat the purpose of data collection.
             */

            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    /**
     * Enables continuing on the child nodes for the current node
     */
    void enableContinueOnChildren()
    {
        continueOnChildren = true;
    }

    /**
     * Returns whether the data provisioning will continue on the current node's children after providing for the
     * current node.
     *
     * @return  continue on children?
     */
    boolean isContinueOnChildren()
    {
        return continueOnChildren;
    }

    ViewData getViewData()
    {
        return viewData;
    }

    public String getProcessName()
    {
        return state.getExecution().getProcessName();
    }

    public ScopedContext getScopedContext()
    {
        return state.getScopedContext();
    }

    public Map<String, Object> getVars(ComponentModel componentModel)
    {
        if (componentModel == null)
        {
            throw new IllegalArgumentException("componentModel can't be null");
        }


        Map<String, Object> vars = new HashMap<>();
        try
        {
            VariableResolutionEnvironment variableResolutionEnvironment = new VariableResolutionEnvironment
                (componentModel, runtimeContext.getLocationParams());

            ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
            if (componentRegistration == null)
            {
                throw new IllegalStateException("No component registration for " + componentModel);
            }

            Map<String, String> varExpressions = componentRegistration.getDescriptor().getVars();

            if (varExpressions != null)
            {
                for (Map.Entry<String, String> entry : varExpressions.entrySet())
                {
                    String varName = entry.getKey();
                    String expression = entry.getValue();

                    ASTExpression astExpression = ExpressionParser.parse(expression);
                    Object result = expressionService.evaluate(astExpression, variableResolutionEnvironment);
                    vars.put(varName, result);
                }
            }

            if (componentModel.equals(overridden))
            {
                vars.putAll(varsOverride);
            }

            return vars;
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    public void registerTranslation(String tag)
    {
        viewData.registerTranslation(tag);
    }

    public void registerTranslations(DomainType type)
    {
        registerTranslation(type.getName());

        for (DomainProperty domainProperty : type.getProperties())
        {
            registerTranslation(domainProperty.getTranslationTag());
        }
    }


    public static class VariableResolutionEnvironment
        extends ExpressionEnvironment
    {

        private final ComponentModel componentModel;

        private final Map<String, Object> locationParams;


        public VariableResolutionEnvironment(ComponentModel componentModel, Map<String, Object> locationParams)
        {
            this.componentModel = componentModel;
            this.locationParams = locationParams;
        }



        @Override
        protected boolean logicalOperatorsAllowed()
        {
            return true;
        }


        @Override
        protected boolean comparatorsAllowed()
        {
            return true;
        }


        @Override
        protected boolean complexLiteralsAllowed()
        {
            return true;
        }


        @Override
        protected boolean arithmeticOperatorsAllowed()
        {
            return true;
        }


        @Override
        protected boolean expressionSequenceAllowed()
        {
            return false;
        }


        @Override
        public Object undefinedOperation(ExpressionContext<ExpressionEnvironment> ctx, ASTFunction node, Object chainObject)
        {
            if (node.getName().equals("param"))
            {
                if (node.jjtGetNumChildren() != 1 && node.jjtGetNumChildren() != 2)
                {
                    throw new IllegalStateException("Operation prop() takes one or two arguments : (name, default)");
                }

                String name = node.jjtGetChild(0).jjtAccept(this, null).toString();

                Object value = locationParams.get(name);
                if (value == null && node.jjtGetNumChildren() == 2)
                {
                    value = node.jjtGetChild(1).jjtAccept(this, null).toString();
                }
                return value;
            }
            if (node.getName().equals("prop"))
            {
                if (node.jjtGetNumChildren() != 1)
                {
                    throw new IllegalStateException("Operation prop() takes exactly one argument");
                }

                Object result = node.jjtGetChild(0).jjtAccept(this, null);
                if (result instanceof String)
                {
                    String propName = (String) result;
                    AttributeValue attribute = componentModel.getAttribute(propName);
                    if (attribute == null)
                    {
                        return null;
                    }

                    ASTExpression astExpression = attribute.getAstExpression();
                    if (astExpression != null)
                    {
                        return astExpression.jjtAccept(this, null);
                    }
                    return attribute.getValue();
                }
                else
                {
                    throw new VariableResolutionException("Invalid prop operator argument: " + result);
                }
            }
            return super.undefinedOperation(ctx, node, chainObject);
        }
    }


}

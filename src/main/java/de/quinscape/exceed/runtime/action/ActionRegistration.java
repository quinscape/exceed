package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

/**
 * Encapsulates the information for a single action method within an action component bean.
 */
public interface ActionRegistration
{
    /**
     * Returns the property model for the return type for the action registration
     *
     * @return  return type of the action
     */
    PropertyModel getReturnType();

    /**
     * Returns a list of formal parameter property models for the action. This is the list of all parameters for which
     * no {@link ParameterProvider} was registered, in the order in which they appear in the original Java method.
     *
     * @return list of action parameter types
     */
    List<DomainProperty> getParameterModels();

    /**
     * Executes the action with the given arguments and optionally converts the result.
     * <p>
     *     The arguments are expected in Java-side JSON equivalent values, their types must fit the underlying action registration.
     * </p>
     *
     *
     * @param runtimeContext    runtime context
     * @param args              list of action arguments.
     *
     * @return  result or converted result
     *
     * @throws InvalidActionParameterException      in case an argument does not match the expected type  
     */
    ActionResult execute(RuntimeContext runtimeContext, ActionParameters args) throws InvalidActionParameterException;


    boolean isVarArgs();

    String getActionName();

    boolean isServerSide();

    String getDescription();
}

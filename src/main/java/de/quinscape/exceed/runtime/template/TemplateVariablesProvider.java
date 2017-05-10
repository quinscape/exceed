package de.quinscape.exceed.runtime.template;

import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.Map;

/**
 * Implemented by classes that want to add base template variables or override the default values.
 * <p>
 *  Implementations in the spring context are automatically picked up.
 * </p>
 */
public interface TemplateVariablesProvider
{

    /**
     * Provides or overrides base template variables.
     *
     * @see TemplateVariables
     * @param runtimeContext    current runtime context
     * @param model             model map for the base template prefilled with the default values.
     */
    void provide(RuntimeContext runtimeContext, Map<String, Object> model);
}

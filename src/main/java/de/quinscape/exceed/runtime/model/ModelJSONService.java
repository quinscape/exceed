package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;

/**
 * Converts model classes to JSON and back.
 */
public interface ModelJSONService
{
    /**
     * Converts the given model to JSON.
     *
     * @param model     model or collection of models
     *
     * @return  JSON string
     */
    String toJSON(Object model);

    /**
     * Converts the given view model to JSON.
     *
     * @param application   runtime application
     * @param model         view model
     * @param jsonFormat    JSON format for view
     *
     * @return  JSON string
     */
    String toJSON(RuntimeApplication application, View model, JSONFormat jsonFormat);

    /**
     * Creates a model instance from the given JSON object
     *
     * @param cls       target class
     * @param json      JSON string
     * @param <T>       target class
     * @return model
     *
     * @throws IllegalArgumentException If the type of the JSON and the model don't match.
     */
    <T> T toModel(Class<T> cls, String json) throws IllegalArgumentException;
}

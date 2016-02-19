package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.view.View;

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
     * @param model     view model
     *
     * @return  JSON string
     */
    String toJSON(View model, JSONFormat jsonFormat);

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

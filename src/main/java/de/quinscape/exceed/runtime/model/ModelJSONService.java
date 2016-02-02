package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.view.View;

/**
 * Converts model classes to JSON and back.
 */
public interface ModelJSONService
{
    /**
     * Base package for all model classes.
     */
    public final static String MODEL_PACKAGE = Model.class.getPackage().getName();

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
     * Converts the given JSON to the corresponding model.
     *
     * @param json  JSON string
     * @param <M>   type will reflect the "_type" attribute of the JSON resolved against the currently registered domain
     *              typwes.
     * @return  typed model
     */
    <M extends Model> M toModel(String json);

    /**
     * Creates a model instance from the given JSON object
     *
     * @param cls       target class
     * @param json      JSON string
     * @param <M>       target class
     * @return model
     *
     * @throws IllegalArgumentException If the type of the JSON and the model don't match.
     */
    <M extends Model> M toModel(Class<M> cls, String json) throws IllegalArgumentException;
}

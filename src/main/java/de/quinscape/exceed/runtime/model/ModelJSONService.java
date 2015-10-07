package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Model;

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
     * @param model     model
     * @param <M>       model type
     * @return  JSON string
     */
    <M extends Model> String toJSON(M model);

    /**
     * Converts the given JSON to the corresponding model.
     *
     * @param json  JSON string
     * @param <T>   type will reflect the "_type" attribute of the JSON resolved against the currently registered domain
     *              typwes.
     * @return  typed model
     */
    <T extends Model> T toModel(String json);

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

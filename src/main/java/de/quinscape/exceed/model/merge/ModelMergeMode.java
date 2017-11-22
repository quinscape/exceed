package de.quinscape.exceed.model.merge;

/**
 * Controls the merge behavior for models and model properties.
 */
public enum ModelMergeMode
{
    /**
     * The model or property replaces the model or property at the same location.
     */
    REPLACE,
    
    /**
     * The model is merged recursively with the model at the same location. Is property is merged with its corresponding
     * property in the other model. For complex types the merge recurses into those collections and model types. The merge
     * strategy might be set to {@link #REPLACE} on property methods of the the merged type or on the embedded types. List
     * properties merged by concatenating the lists.
     */
    DEEP
}

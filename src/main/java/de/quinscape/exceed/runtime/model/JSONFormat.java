package de.quinscape.exceed.runtime.model;

/**
 * The JSON format we use often differs between the format it is stored on disk, the model, and what we need internally
 *
 */
public enum JSONFormat
{
    /**
     * External JSON format
     */
    EXTERNAL,
    /**
     * Internal JSON format
     */
    INTERNAL
}

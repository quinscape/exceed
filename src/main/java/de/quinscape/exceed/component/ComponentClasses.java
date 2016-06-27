package de.quinscape.exceed.component;

/**
 * These are special component descriptor signal classes that are used for purposes other than completion checking.
 */
public interface ComponentClasses
{
    /**
     * If set, the js component will receive "model" and "viewModel" props pointing to its own model and the view model
     * respectively.
     */
    String MODEL_AWARE = "model-aware";
}

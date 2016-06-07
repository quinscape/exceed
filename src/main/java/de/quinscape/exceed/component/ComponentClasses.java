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

    /**
     * If set, the component can have "visibleIf" attribute that determines if the element is rendered or not. If such an
     * attribute is not given, the element is supposed to be visible.
     *
     * visibility is handled by view-renderer.js.
     */
    String VISIBLE_IF = "visible-if";
}

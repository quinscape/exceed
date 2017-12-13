package de.quinscape.exceed.model.component;

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
     * Signal class that marks form elements (using the FormElement HOC). Must be combined with "model-aware".  
     */
    String FIELD = "field";

    /**
     * Marks components always needing an id. If the user does not define an id, one is automatically generated. This
     * happens automatically for "field" annotated components, too.
     */
    String NEEDS_ID = "needs-id";

    /**
     * Ensures that queries of the component automatically add the ids fields.
     */
    String QUERY_IDS = "query-ids";

    /**
     * Contains form fields and provides their DataCursors. The container itself has a "data" cursor attribute that
     * accesses a context variable
     */
    String FORM_CONTAINER = "form-container";

    /**
     * Can provide form configuration properties for the form children inside. Might inherit them from a parent FORM_CONFIG_CONTAINER.
     */
    String FORM_CONFIG_CONTAINER = "form-config-provider";

    /**
     * The context provided is part of an iteration over a parent list.
     */
    String ITERATIVE_CONTEXT = "iterative-context";

}

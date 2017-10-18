/**
 * These are special component descriptor signal classes that are used for purposes other than completion checking.
 *
 * MODULE IS AUTO-GENERATED. DO NOT EDIT.
 * ( Edit de.quinscape.exceed.model.component.ComponentClasses instead )
 *
 * @type {{MODEL_AWARE: string, FIELD: string, NEEDS_ID: string, QUERY_IDS: string, FORM_CONTAINER: string, FORM_CONFIG_CONTAINER: string, ITERATIVE_CONTEXT: string}}
 */
const ComponentClasses = {
    /**
    If set, the js component will receive "model" and "viewModel" props pointing to its own model and the view model
      respectively.
    */
    MODEL_AWARE: "model-aware",
    /**
    Signal class that marks form elements (using the FormElement HOC). Must be combined with "model-aware".
    */
    FIELD: "field",
    /**
    Marks components always needing an id. If the user does not define an id, one is automatically generated. This
      happens automatically for "field" annotated components, too.
    */
    NEEDS_ID: "needs-id",
    /**
    Ensures that queries of the component automatically add the ids fields.
    */
    QUERY_IDS: "query-ids",
    /**
    Contains form fields and provides their DataCursors. The container itself has a "data" cursor attribute that
      accesses a context variable
    */
    FORM_CONTAINER: "form-container",
    /**
    Can provide form configuration properties for the form children inside. Might inherit them from a parent FORM_CONFIG_CONTAINER.
    */
    FORM_CONFIG_CONTAINER: "form-config-provider",
    /**
    The context provided is part of an iteration over a parent list.
    */
    ITERATIVE_CONTEXT: "iterative-context"
};

export default ComponentClasses

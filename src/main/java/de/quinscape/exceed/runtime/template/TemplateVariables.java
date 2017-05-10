package de.quinscape.exceed.runtime.template;

import de.quinscape.exceed.runtime.application.RuntimeApplication;

/**
 * Contains the variable names provided for our one HTML template ( /resources/template/template.html )
 */
public final class TemplateVariables
{
    private TemplateVariables()
    {
        // no instances
    }

    /**
     * Context-path of the exceed servlet application (may contain multiple apps)
     */
    public static String CONTEXT_PATH = "CONTEXT_PATH";
    /**
     * Current locale code
     */
    public static String LOCALE = "LOCALE";
    /**
     * View title
     */
    public static String TITLE = "TITLE";
    /**
     * Script tags to render for inclusion of base bundles / libraries. Provided as block by ApplicationController and
     * LoginController
     */
    public static String SCRIPTS = "SCRIPTS";
    /**
     * Content to insert into the root element. For now Error message markup in the case of an error view. In the future server-side prerendering of js output.
     */
    public static String CONTENT = "CONTENT";
    public static String CONTENT_BEFORE = "CONTENT_BEFORE";
    public static String CONTENT_AFTER = "CONTENT_AFTER";
    public static String HEAD = "HEAD";

    /**
     * The current view data JSON block. Will contain a ComponentData object per injected-component and the runtime info block under {@link RuntimeApplication#RUNTIME_INFO_NAME}
     */
    public static String VIEW_DATA = "VIEW_DATA";

    public static String APP_NAME = "APP_NAME";
}

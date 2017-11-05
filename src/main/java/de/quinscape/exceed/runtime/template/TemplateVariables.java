package de.quinscape.exceed.runtime.template;

import de.quinscape.exceed.runtime.application.RuntimeApplication;

/**
 * Contains the variable names provided for our one HTML template ( /resources/template/template.html )
 */
public interface TemplateVariables
{
    String APP_NAME = "APP_NAME";

    String CONTENT_AFTER = "CONTENT_AFTER";
    String CONTENT_BEFORE = "CONTENT_BEFORE";
    /**
     * Content to insert into the root element. Currently In the future server-side prerendering of js output.
     */
    String CONTENT = "CONTENT";

    /**
     * Context-path of the exceed servlet application (may contain multiple apps)
     */
    String CONTEXT_PATH = "CONTEXT_PATH";

    /**
     * Script tags to render for inclusion of base bundles / libraries. Provided as block by the system, configurable
     * in 
     */
    String SCRIPTS = "SCRIPTS";

    /**
     * Addition HTML content to insert into the &lt;head&gt;
     *
     * @see
     */
    String HEAD = "HEAD";

    /**
     * Current locale code (w3c Format)
     */
    String LOCALE = "LOCALE";

    /**
     * View title
     */
    String TITLE = "TITLE";

    /**
     * The current view data JSON block. Will contain a ComponentData object per injected-component and the runtime info block under {@link RuntimeApplication#RUNTIME_INFO_NAME}
     */
    String VIEW_DATA = "VIEW_DATA";

}

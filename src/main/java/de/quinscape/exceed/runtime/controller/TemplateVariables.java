package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.application.RuntimeApplication;

/**
 * Contains the variable names provided for our one HTML template ( /resources/template/template.html )
 */
public interface TemplateVariables
{
    /**
     * Context-path of the exceed servlet application (may contain multiple apps)
     */
    String CONTEXT_PATH = "CONTEXT_PATH";
    /**
     * Current user name
     */
    String USER_NAME = "USER_NAME";
    /**
     * Current user roles
     */
    String USER_ROLES = "USER_ROLES";
    /**
     * System information JSON block
     */
    String SYSTEM_INFO = "SYSTEM_INFO";
    /**
     * Websocket connection id prepared for the page context
     */
    String CONNECTION_ID = "CONNECTION_ID";

    /**
     * Current CSFR Token
     */
    String CSRF_TOKEN = "CSRF_TOKEN";
    /**
     * CSFR Form parameter name
     */
    String CSRF_PARAMETER_NAME = "CSRF_PARAMETER_NAME";
    /**
       CSFR HTTP header name
     */
    String CSRF_HEADER_NAME = "CSRF_HEADER_NAME";
    /**
     * Application name (might be automatically chosen default app)
     */
    String APP_NAME = "APP_NAME";
    /**
     * Current locale code
     */
    String LOCALE = "LOCALE";
    /**
     * View title
     */
    String TITLE = "TITLE";
    /**
     * Script tags to render for inclusion of base bundles / libraries. Provided as block by ApplicationController and
     * LoginController
     */
    String SCRIPTS = "SCRIPTS";
    /**
     * Content to insert into the root element. For now Error message markup in the case of an error view. In the future server-side prerendering of js output.
     */
    String CONTENT = "CONTENT";

    /**
     * The current view model JSON block.
     */
    String VIEW_MODEL = "VIEW_MODEL";

    /**
     * The current view data JSON block. Will contain a ComponentData object per injected-component and the runtime info block under {@link RuntimeApplication#RUNTIME_INFO_NAME}
     */
    String VIEW_DATA = "VIEW_DATA";
}

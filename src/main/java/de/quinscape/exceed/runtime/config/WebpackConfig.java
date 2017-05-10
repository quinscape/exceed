package de.quinscape.exceed.runtime.config;

/**
 * Contains constants for the webpack configuration.
 */
public final class WebpackConfig
{


    private WebpackConfig()
    {
        // no instances
    }


    /**
     * Entry point module for the exceed editor
     */
    public final static String EDITOR_MAIN_MODULE = "./editor-main";

    /**
     * Bundle set for application pages (in-page code editor loaded dynamically).
     */
    public final static String APP_BUNDLES = "common,app";

    /**
     * Bundle set for the exceed editor
     */
    public final static String EDITOR_BUNDLES = "common,editor";
    public final static String DOCS_BUNDLES = "common,docs";

    public static final String DOCS_MAIN_MODULE = "./docs-main";
}

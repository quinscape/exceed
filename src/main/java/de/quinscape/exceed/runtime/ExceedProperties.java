package de.quinscape.exceed.runtime;

/**
 * Contains system/spring environment property names or prefixes.
 */
public interface ExceedProperties
{
    /**
     * The full path of a exceed base library development directory / git repository checkout.
     *
     * If this is set, the system will read the base extension from the src/main/base folder of the directory.
     */
    String LIBRARY_SOURCE_SYSTEM_PROPERTY = "exceed.library.source";

    /**
     * Environment property prefix to control the active stages for an exceed application.
     *
     * Example:
     * <pre title="Stage definition example">
     *     -Dexceed.stages.myapp=default,myStage
     * </pre>
     */
    String STAGES_PROPERTY_PREFIX = "exceed.stages.";

    /**
     * Environment property prefix to declare an extra extension directory for an application.
     * <pre title="Extra extension example">
     *     -Dexceed.extra.myapp=/home/user/extra-extension/
     * </pre>
     *
     * The extension directory will read and merged in as top priority extension.
     */
    String EXTRA_PROPERTY_PREFIX = "exceed.extra.";


    /**
     *  Environment property prefix to define the location where a schema update mode DUMP writes the current schema
     *  dump to.
     *
     * <pre title="Schema dump location example">
     *     -Dexceed.schemadump.myapp=/home/user/schema.sql
     * </pre>
     */
    String SCHEMA_DUMP_PREFIX = "exceed.schemadump.";
}

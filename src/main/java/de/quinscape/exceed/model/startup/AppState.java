package de.quinscape.exceed.model.startup;

import com.google.common.collect.ImmutableList;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.startup.AppStateBuilder;
import org.svenson.JSONParameter;

import java.util.List;

/**
 * Application startup configuration.
 */
public class AppState
    implements Cloneable
{
    private static final List<String> DEFAULT_STAGES = ImmutableList.of("default");

    private final String name;

    private final String path;

    private final ApplicationStatus status;

    private final List<String> extensions;

    private final List<String> stages;


    private final String domainVersion;


    //[null, crud, null, null, crud-example, null, null, null]
    public AppState(
        @JSONParameter("name")
            String name,

        @JSONParameter("path")
            String path,

        @JSONParameter("status")
            ApplicationStatus status,

        @JSONParameter("extensions")
            List<String> extensions,

        @JSONParameter("domainVersion")
            String domainVersion,

        @JSONParameter("defaultApp")
            String defaultApp,

        @JSONParameter("stages")
            List<String> stages
    )
    {
        this.name = name;
        this.path = path;
        this.status = status != null ? status : ApplicationStatus.DEVELOPMENT;
        this.extensions = extensions;
        this.domainVersion = domainVersion;
        this.stages = stages != null ? stages : DEFAULT_STAGES;
    }


    /**
     * Application name.
     */
    public String getName()
    {
        return name;
    }


    /**
     * Base path for the extension. Currently filled by the system automatically depending on whether the
     * application is running as exploded-war or not.
     */
    public String getPath()
    {
        return path;
    }


    /**
     * Application status enum.
     */
    public ApplicationStatus getStatus()
    {
        return status;
    }


    /**
     * List of extension names for this application.
     */
    public List<String> getExtensions()
    {
        return extensions;
    }


    /**
     * Version of the current domain model. WIP
     */
    public String getDomainVersion()
    {
        return domainVersion;
    }


    public AppStateBuilder buildCopy()
    {
        return new AppStateBuilder(this);
    }


    public static AppStateBuilder builder()
    {
        return new AppStateBuilder();
    }


    /**
     * Array with the active stage names. Default is <code>["default"]</code>. Can be overridden by setting a system property
     * "exceed.stages.myapp" ( with "myapp" being the name of the app to set the stages for). The value must be a comma separated
     * list of stage names in that case.
     *
     * <pre title="system property example">
     *  -Dexeed.stages.myapp=default,prod
     * </pre>
     *
     */
    public List<String> getStages()
    {
        return stages;
    }
}

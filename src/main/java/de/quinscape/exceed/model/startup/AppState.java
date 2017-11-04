package de.quinscape.exceed.model.startup;

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
    private final String name;
    private final String path;
    private final ApplicationStatus status;
    private final List<String> extensions;
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
        String defaultApp

    )
    {
        this.name = name;
        this.path = path;
        this.status = status != null ? status : ApplicationStatus.DEVELOPMENT;
        this.extensions = extensions;
        this.domainVersion = domainVersion;
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
}

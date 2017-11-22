package de.quinscape.exceed.runtime.startup;

import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.runtime.application.ApplicationStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AppStateBuilder
{
    private String name;

    private String path;

    private ApplicationStatus status;

    private List<String> extensions;

    private String domainVersion;

    private String defaultApp;

    private List<String> stages;


    public AppStateBuilder()
    {

    }

    public AppStateBuilder(AppState state)
    {
        name = state.getName();
        path = state.getPath();
        status = state.getStatus();
        extensions = state.getExtensions();
        domainVersion = state.getDomainVersion();
    }

    public AppStateBuilder withName(String name)
    {
        this.name = name;
        return this;
    }


    public AppStateBuilder withPath(String path)
    {
        this.path = path;
        return this;
    }


    public AppStateBuilder withStatus(ApplicationStatus status)
    {
        this.status = status;
        return this;
    }


    public AppStateBuilder withExtensions(Collection<String> extensions)
    {
        this.extensions = new ArrayList<>(extensions);
        return this;
    }


    public AppStateBuilder withExtensions(String... extensions)
    {
        this.extensions = new ArrayList<>(Arrays.asList(extensions));
        return this;
    }


    public AppStateBuilder withDomainVersion(String domainVersion)
    {
        this.domainVersion = domainVersion;
        return this;
    }


    public AppStateBuilder withDefaultApp(String defaultApp)
    {
        this.defaultApp = defaultApp;
        return this;
    }


    public AppStateBuilder withStages(List<String> stages)
    {
        this.stages = stages;
        return this;
    }


    public AppState build()
    {
        return new AppState(name, path, status, extensions, domainVersion, defaultApp, stages);
    }
}

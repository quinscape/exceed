package de.quinscape.exceed.model;

import de.quinscape.exceed.runtime.component.StaticFunctionReferences;

/**
 * Encapsulates application meta data.
 * <p>
 * It contains all the things of the application model that are not primary artifacts (domain models, processes, views
 * etc) but secondary information about the application. Results of code analysis, graphical editor object layouts etc.
 * </p>
 */
public class ApplicationMetaData
{
    private StaticFunctionReferences staticFunctionReferences;

    private DomainEditorViews domainEditorViews;


    public ApplicationMetaData()
    {
        domainEditorViews = new DomainEditorViews();
        domainEditorViews.setName("domain");
    }


    public StaticFunctionReferences getStaticFunctionReferences()
    {
        return staticFunctionReferences;
    }


    public void setStaticFunctionReferences(StaticFunctionReferences staticFunctionReferences)
    {
        this.staticFunctionReferences = staticFunctionReferences;
    }


    public DomainEditorViews getDomainEditorViews()
    {
        return domainEditorViews;
    }


    public void setDomainEditorViews(DomainEditorViews domainEditorViews)
    {
        this.domainEditorViews = domainEditorViews;
    }
}

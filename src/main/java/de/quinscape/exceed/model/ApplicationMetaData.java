package de.quinscape.exceed.model;

import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.component.StaticFunctionReferences;

/**
 * Encapsulates application meta data.
 * <p>
 * It contains all the things of the application model that are not primary artifacts (domain models, processes, views
 * etc) but secondary information about the application. Results of code analysis, graphical editor object layouts etc.
 * </p>
 */
public class ApplicationMetaData
{
    private final ApplicationModel applicationModel;

    private StaticFunctionReferences staticFunctionReferences;

    private DomainEditorViews domainEditorViews;

    private final ScopeMetaModel scopeMetaModel;

    public ApplicationMetaData(ApplicationModel applicationModel)
    {
        this.applicationModel = applicationModel;

        domainEditorViews = new DomainEditorViews();
        domainEditorViews.setName("domain");

        scopeMetaModel = new ScopeMetaModel(applicationModel);
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


    public ScopeMetaModel getScopeMetaModel()
    {
        return scopeMetaModel;
    }
}

package de.quinscape.exceed.model;

import de.quinscape.exceed.model.annotation.Internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Internal
public class DomainEditorViews
    extends AbstractTopLevelModel
{
    private static final Map<String, DomainEditorView> DEFAULT_VIEWS = createDefault();


    private static Map<String, DomainEditorView> createDefault()
    {
        Map<String, DomainEditorView> map = new HashMap<>();
        map.put("Default", null);
        return Collections.unmodifiableMap(map);
    }


    private Map<String, DomainEditorView> views = DEFAULT_VIEWS;


    public Map<String, DomainEditorView> getViews()
    {
        return views;
    }


    public void setViews(Map<String, DomainEditorView> views)
    {
        this.views = views;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        throw new UnsupportedOperationException();
    }
}

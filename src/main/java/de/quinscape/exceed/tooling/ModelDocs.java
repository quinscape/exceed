package de.quinscape.exceed.tooling;

import java.util.List;
import java.util.Map;

public class ModelDocs
{
    private final List<String> topLevelTypes;

    private final Map<String, ModelDoc> docs;


    public ModelDocs(List<String> topLevelTypes, Map<String, ModelDoc> docs)
    {
        this.topLevelTypes = topLevelTypes;
        this.docs = docs;
    }


    public List<String> getTopLevelTypes()
    {
        return topLevelTypes;
    }


    public Map<String, ModelDoc> getDocs()
    {
        return docs;
    }
}

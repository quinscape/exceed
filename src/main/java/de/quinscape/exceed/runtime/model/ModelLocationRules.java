package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;

import java.util.List;

public class ModelLocationRules
{
    private final List<ModelLocationRule> modelLocationRules;


    public ModelLocationRules(List<ModelLocationRule> modelLocationRules)
    {
        this.modelLocationRules = modelLocationRules;
    }


    public List<ModelLocationRule> getRules()
    {
        return modelLocationRules;
    }

    public Class<TopLevelModel> matchType(String path)
    {
        for (ModelLocationRule rule : modelLocationRules)
        {
            if (rule.matches(path))
            {
                final String type = rule.getType();
                return Model.getType(type);
            }
        }
        return null;
    }
}

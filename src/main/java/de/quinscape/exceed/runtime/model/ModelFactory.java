package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelFactory
    implements org.svenson.ObjectFactory<Model>
{
    private static Logger log = LoggerFactory.getLogger(ModelFactory.class);

    @Override
    public boolean supports(Class<Model> cls)
    {
        return Model.class.isAssignableFrom(cls);
    }

    @Override
    public Model create(Class<Model> cls)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Create {}", cls.getName());
            }
            return cls.newInstance();
        }
        catch (Exception e)
        {
            throw new ModelCreationException("Error creating model with class " + cls, e);
        }
    }
}

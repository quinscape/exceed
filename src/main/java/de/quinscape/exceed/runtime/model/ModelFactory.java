package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.view.ComponentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelFactory
    implements org.svenson.ObjectFactory<Object>
{
    private static Logger log = LoggerFactory.getLogger(ModelFactory.class);


    @Override
    public boolean supports(Class<Object> cls)
    {
        return Model.class.isAssignableFrom(cls) || ComponentModel.class.isAssignableFrom(cls);
    }

    @Override
    public Object create(Class<Object> cls)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Create {}", cls.getName());
            }
            Object model = cls.newInstance();

//            if (model instanceof ComponentModel)
//            {
//                ((ComponentModel) model).setComponentIdService(componentIdService);
//
//            }

            return model;
        }
        catch (Exception e)
        {
            throw new ModelCreationException("Error creating model with class " + cls, e);
        }
    }
}

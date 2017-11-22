package de.quinscape.exceed.model.context;

import de.quinscape.exceed.model.annotation.DocumentedCollection;
import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.merge.ModelMergeMode;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.Map;

/**
 * Model for a scoped context (e.g. application context, session context, process context etc).
 */
@MergeStrategy(ModelMergeMode.DEEP)
public class ContextModel
{
    private Map<String, ScopedPropertyModel> properties;

    private static void init(Map<String, ScopedPropertyModel> elems)
    {
        for (Map.Entry<String, ScopedPropertyModel> entry : elems.entrySet())
        {
            String name = entry.getKey();
//            if (ProcessContext.RESERVED_NAMES.contains(name))
//            {
//                throw new IllegalStateException("'" + name + "' is a reserved name.");
//            }

            final ScopedPropertyModel scopedPropertyModel = entry.getValue();
            PropertyModel.initDefaults(scopedPropertyModel);

            final String existingName = scopedPropertyModel.getName();
            if (existingName != null && !existingName.equals(name))
            {
                throw new InconsistentModelException(
                    "Scoped property '" + name + "' declares a different name '" + existingName + "'. " +
                    "You don't need to define name properties, they receive the name from their map key automatically." +
                    "If you define a name is must be the same as the map key.");
            }
            scopedPropertyModel.setName(name);
        }
    }


    /**
     * List of properties for this context.
     */
    @JSONTypeHint(ScopedPropertyModel.class)
    @DocumentedCollection(keyDesc = "propertyName")
    public Map<String, ScopedPropertyModel> getProperties()
    {
        if (properties == null)
        {
            return Collections.emptyMap();
        }
        return properties;
    }


    public void setProperties(Map<String, ScopedPropertyModel> properties)
    {
        init(properties);
        this.properties = properties;
    }
}

package de.quinscape.exceed.runtime.service.client;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.domain.DomainTypesRegistry;
import de.quinscape.exceed.runtime.editor.domain.DomainTypeQuery;
import de.quinscape.exceed.runtime.js.JsExpressionRenderer;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.util.JSONBuilder;

/**
 * Result based on an object that is automatically converted to JSON with the default generator.
 */
public class ModelClientData
    implements ClientData
{

    private final JsExpressionRenderer renderer;

    private final DomainTypesRegistry registry;

    private final TopLevelModel topLevelModel;


    public ModelClientData(JsExpressionRenderer renderer, DomainTypesRegistry registry, TopLevelModel topLevelModel)
    {
        if (renderer == null)
        {
            throw new IllegalArgumentException("renderer can't be null");
        }

        if (registry == null)
        {
            throw new IllegalArgumentException("registry can't be null");
        }

        if (topLevelModel == null)
        {
            throw new IllegalArgumentException("topLevelModel can't be null");
        }

        this.renderer = renderer;
        this.registry = registry;
        this.topLevelModel = topLevelModel;
    }


    @Override
    public String getJSON()
    {
        final JSONBuilder builder = JSONBuilder.buildObject();

        final DomainType domainType = registry.getDomainTypes().get(Model.getType(topLevelModel.getClass()));

        

        return builder.output();
    }


    private void construct(JSONBuilder builder, Object data)
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(data.getClass());

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (info.isIgnore())
            {
                continue;
            }

            final String name = info.getJsonName();
            final Object value = JSONUtil.DEFAULT_UTIL.getProperty(data, name);
            if (value == null && info.isIgnoreIfNull())
            {
                continue;
            }

            final Object property = value;
            builder.property(name, property);

        }

    }
}

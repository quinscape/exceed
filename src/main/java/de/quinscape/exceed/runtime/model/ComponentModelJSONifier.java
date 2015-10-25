package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.svenson.JSON;
import org.svenson.JSONCharacterSink;
import org.svenson.SinkAwareJSONifier;

import java.util.Iterator;
import java.util.List;

public class ComponentModelJSONifier
    implements SinkAwareJSONifier
{
    private final ComponentJSONFormat jsonFormat;

    private final JSON generator;


    public ComponentModelJSONifier(JSON generator, ComponentJSONFormat componentJSONFormat)
    {
        if (generator == null)
        {
            throw new IllegalArgumentException("generator can't be null");
        }

        if (componentJSONFormat == null)
        {
            throw new IllegalArgumentException("componentJSONFormat can't be null");
        }

        this.jsonFormat = componentJSONFormat;
        this.generator = generator;
    }


    @Override
    public void writeToSink(JSONCharacterSink sink, Object o)
    {
        ComponentModel componentModel = (ComponentModel) o;

        Attributes attrs = componentModel.getAttrs();

        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();

        sink.append("{\"name\":");

        generator.quote(sink, componentModel.getName());

        if (attrs != null)
        {
            sink.append(",\"attrs\":{");
            boolean first = true;
            for (String name : attrs.getNames())
            {
                boolean ignoreAttribute = jsonFormat == ComponentJSONFormat.INTERNAL && isServerOnlyProperty(componentRegistration, name);

                if (!ignoreAttribute)
                {
                    if (!first)
                    {
                        sink.append(',');
                    }
                    generator.quote(sink, name);
                    sink.append(':');
                    generator.dumpObject(sink, attrs.getAttribute(name).getValue());
                    first = false;
                }
            }
            sink.append("}");
        }

        List<ComponentModel> kids = componentModel.getKids();
        if (kids == null)
        {
            sink.append("}");
        }
        else
        {
            sink.append(",\"kids\":[");

            for (Iterator<ComponentModel> iterator = kids.iterator(); iterator.hasNext(); )
            {
                ComponentModel kid = iterator.next();
                generator.dumpObject(sink, kid);

                if (iterator.hasNext())
                {
                    sink.append(",");
                }
            }

            sink.append("]}");
        }
    }


    private boolean isServerOnlyProperty(ComponentRegistration componentRegistration, String name)
    {
        if (componentRegistration != null)
        {
            PropDeclaration propDecl = componentRegistration.getDescriptor().getPropTypes().get(name);
            return propDecl != null && !propDecl.isClient();
        }
        return false;
    }


    @Override
    public String toJSON(Object o)
    {
        throw new UnsupportedOperationException();
    }
}

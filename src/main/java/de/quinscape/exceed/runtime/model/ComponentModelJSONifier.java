package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionDumpVisitor;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
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

    private final JSON generator;

    private final JSONFormat jsonFormat;


    public ComponentModelJSONifier(JSON generator, JSONFormat jsonFormat)
    {
        if (generator == null)
        {
            throw new IllegalArgumentException("generator can't be null");
        }

        this.generator = generator;
        this.jsonFormat = jsonFormat;
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
                if (!first)
                {
                    sink.append(',');
                }
                generator.quote(sink, name);
                sink.append(':');
                generator.dumpObject(sink, attrs.getAttribute(name).getValue());
                first = false;
            }
            sink.append("}");

            if (jsonFormat == JSONFormat.CLIENT)
            {
                sink.append(",\"exprs\":{");
                first = true;
                for (String name : attrs.getNames())
                {
                    AttributeValue attribute = attrs.getAttribute(name);
                    ASTExpression astExpression = attribute.getAstExpression();
                    if (astExpression != null)
                    {
                        if (!first)
                        {
                            sink.append(',');
                        }
                        //ClientExpressionTransformVisitor v = new ClientExpressionTransformVisitor(componentModel, componentId, path);
                        //astExpression.jjtAccept(v, null);

                        generator.quote(sink, name);
                        sink.append(':');
                        generator.dumpObject(sink, "");
                        first = false;
                    }
                }
                sink.append("}");
            }

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



    @Override
    public String toJSON(Object o)
    {
        throw new UnsupportedOperationException();
    }
}

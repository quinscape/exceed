package de.quinscape.exceed.runtime.template;

import de.quinscape.exceed.runtime.util.RequestUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public final class Placeholder
    implements TemplatePart
{
    private final String name;


    public Placeholder(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        this.name = name;
    }


    @Override
    public void write(OutputStream os, Map<String, Object> model) throws IOException
    {
        final Object value = model.get(name);
        if (value != null)
        {
            os.write(value.toString().getBytes(RequestUtil.UTF_8));
        }
    }
}

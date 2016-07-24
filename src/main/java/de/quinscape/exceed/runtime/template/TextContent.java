package de.quinscape.exceed.runtime.template;

import de.quinscape.exceed.runtime.util.RequestUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public final class TextContent
    implements TemplatePart
{
    private final byte[] content;

    public TextContent(String content)
    {
        if (content == null)
        {
            throw new IllegalArgumentException("content can't be null");
        }
        this.content = content.getBytes(RequestUtil.UTF_8);
    }

    @Override
    public void write(OutputStream os, Map<String, Object> model) throws IOException
    {
        os.write(content);
    }
}

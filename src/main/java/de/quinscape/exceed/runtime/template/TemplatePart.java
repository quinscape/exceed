package de.quinscape.exceed.runtime.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface TemplatePart
{
    void write(OutputStream os, Map<String, Object> model) throws IOException;
}


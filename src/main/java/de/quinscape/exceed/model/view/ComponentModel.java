package de.quinscape.exceed.model.view;

import de.quinscape.exceed.runtime.view.ComponentService;
import de.quinscape.exceed.runtime.view.DataProvider;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.StringBuilderSink;

import javax.annotation.PostConstruct;
import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentModel
{
    private String name;

    private Attributes attrs;

    private List<ComponentModel> kids;

    private String dataProvider;

    private DataProvider dataProviderInstance;

    private ComponentService componentService;

    @JSONProperty(priority = 10)
    public String getName()
    {
        return name;
    }

    @JSONProperty(ignoreIfNull = true, priority = 5)
    public Attributes getAttrs()
    {
        return attrs;
    }

    @JSONProperty(ignoreIfNull = true)
    public List<ComponentModel> getKids()
    {
        return kids;
    }

    public void setKids(List<ComponentModel> kids)
    {
        this.kids = kids;
    }


    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Null-safe list of children
     *
     * @return list of children, might be empty.
     */
    public List<ComponentModel> children()
    {
        if (kids == null)
        {
            return Collections.emptyList();
        }
        return kids;
    }

    @JSONProperty(ignoreIfNull = true)
    public String getDataProvider()
    {
        return dataProvider;
    }

    public void setDataProvider(String dataProvider)
    {
        this.dataProvider = dataProvider;
    }

    public void setDataProviderInstance(DataProvider dataProviderInstance)
    {
        this.dataProviderInstance = dataProviderInstance;
    }

    @JSONProperty(ignore = true)
    public DataProvider getDataProviderInstance()
    {
        return dataProviderInstance;
    }

    @PostConstruct
    public void init()
    {
        if (attrs == null)
        {
            attrs = new Attributes(null);
        }

        if (attrs.getAttribute("id") == null)
        {
            attrs.setGeneratedId(componentService.createId());
        }
    }

    @JSONProperty(ignore = true)
    public String getComponentId()
    {
        AttributeValue value = attrs.getAttribute("id");
        return value != null ? (String) value.getValue() : null;
    }

    public AttributeValue getAttribute(String key)
    {
        return attrs.getAttribute(key);
    }

    public void setAttrs(Attributes attrs)
    {
        this.attrs = attrs;
    }

    @JSONProperty(ignore = true)
    public ComponentService getComponentService()
    {
        return componentService;
    }

    public void setComponentService(ComponentService componentService)
    {
        this.componentService = componentService;
    }

    @Override
    public String toString()
    {
        StringBuilderSink sb = new StringBuilderSink();
        sb.append("<");
        sb.append(name);

        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                sb.append(" ");
                sb.append(name);
                sb.append("=");

                AttributeValue attribute = attrs.getAttribute(name);
                AttributeValueType type = attribute.getType();
                Object value = attribute.getValue();
                if (type == AttributeValueType.COMPLEX)
                {
                    JSON.defaultJSON().dumpObject(sb, value);
                }
                else if (type == AttributeValueType.STRING)
                {
                    JSON.defaultJSON().quote(sb, (String) value);
                }
                else
                {
                    sb.append(value);
                }
            }
        }
        sb.append(">");
        return sb.getContent();
    }
}


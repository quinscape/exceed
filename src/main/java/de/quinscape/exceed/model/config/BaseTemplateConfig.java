package de.quinscape.exceed.model.config;


import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.merge.MergeType;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import org.svenson.AbstractDynamicProperties;
import org.svenson.JSONProperty;

/**
 * Configures constants in the base template. The configuration options listed here are those of the default base template.
 * You can use arbitrary property names here for your own base templates.
 *
 */
@MergeStrategy(MergeType.DEEP)
public class BaseTemplateConfig
    extends AbstractDynamicProperties
    implements TemplateVariables
{
    /**
     * Additional &lt;head&gt; html
     */
    @JSONProperty(HEAD)
    public String getHead()
    {
        return (String) getProperty(HEAD);
    }

    public void setHead(String head)
    {
        setProperty(HEAD, head);
    }


    /**
     * Additional content to add after layout root.
     */
    @JSONProperty(CONTENT_AFTER)
    public String getContentAfter()
    {
        return (String) getProperty(CONTENT_AFTER);
    }


    public void setContentAfter(String contentAfter)
    {
        setProperty(CONTENT_AFTER, contentAfter);
    }


    /**
     * Additional content to add before layout root.
     */
    @JSONProperty(CONTENT_BEFORE)
    public String getContentBefore()
    {
        return (String) getProperty(CONTENT_BEFORE);
    }


    public void setContentBefore(String contentBefore)
    {
        setProperty(CONTENT_BEFORE, contentBefore);
    }

}

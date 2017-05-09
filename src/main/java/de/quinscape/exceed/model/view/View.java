package de.quinscape.exceed.model.view;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.DocumentedMapKey;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.model.JSONFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents a view model within an exceed application
 *
 * The persistent JSON output for this model generated by  {@link de.quinscape.exceed.runtime.model.ClientViewJSONGenerator#toJSON(de.quinscape.exceed.model.ApplicationModel, View, JSONFormat)}  }.
 *
 *
 *
 */
public class View
    extends TopLevelModel
    implements AutoVersionedModel
{
    private final static Logger log = LoggerFactory.getLogger(View.class);

    @Override
    public <I, O> O accept(TopLevelModelVisitor<I, O> visitor, I in)
    {
        return visitor.visit(this, in);
    }

    /**
     * Name of the root content. The root content is the outmost markup in your layout. The default for the root content
     * is {@link LayoutModel#DEFAULT_LAYOUT}
     */
    public static final String ROOT = "root";

    /**
     * Default name for the view model components.  
     */
    public static final String MAIN = "main";

    private Map<String,ComponentModel> content;

    private List<String> comments;

    private boolean synthetic;

    private String processName;

    private String version;

    private ContextModel contextModel;

    private String identityGUID;

    private String layout;

    private AttributeValue title;

    public View()
    {
        content = createEmptyViewWithDefaultLayout();
    }


    private Map<String, ComponentModel> createEmptyViewWithDefaultLayout()
    {
        final Map<String, ComponentModel> content = new HashMap<>();
        content.put(ROOT, LayoutModel.DEFAULT_LAYOUT);
        return content;
    }


    @Internal
    public String getName()
    {
        return super.getName();
    }



    /**
     * Returns the complete content map of this view. The default content is called "main". The "root" content will imported
     * from the layout, by default it just imports the "main" slot.
     *
     * @return content map.
     */
    @JSONTypeHint(ComponentModel.class)
    @DocumentedMapKey("contentName")
    public Map<String, ComponentModel> getContent()
    {
        return content;
    }


    public void setContent(Map<String, ComponentModel> content)
    {
        if (content == null)
        {
            this.content = createEmptyViewWithDefaultLayout();
        }
        else
        {
            final Map<String, ComponentModel> copy = new HashMap<>(content);
            copy.putIfAbsent(ROOT, LayoutModel.DEFAULT_LAYOUT);
            this.content = copy;
        }
    }


    // XXX: do we need comments on views?
    @Internal
    public List<String> getComments()
    {
        return comments;
    }


    public void setComments(List<String> comments)
    {
        this.comments = comments;
    }


    /**
     * UUID uniquely indentifying the current version of the view. Doesn't need to be set, needs to be removed
     * or changed if you change the view manually.
     */
    @Override
    public String getVersionGUID()
    {

        return version;
    }


    @Override
    public void setVersionGUID(String version)
    {
        this.version = version;
    }


    @Internal
    public boolean isSynthetic()
    {
        return synthetic;
    }


    public void setSynthetic(boolean synthetic)
    {
        this.synthetic = synthetic;
    }


    /**
     * Returns the first component model in pre-order traversal
     * that matches the given predicate.
     *
     * @param predicate predicate
     * @return first component model that matches the predicate
     */
    public ComponentModel find(Predicate<ComponentModel> predicate)
    {
        for (ComponentModel componentModel : content.values())
        {
            final ComponentModel found = componentModel.find(predicate);
            if (found != null)
            {
                return found;
            }
        }
        return null;
    }

    @Internal
    public String getProcessName()
    {
        return processName;
    }


    public void setProcessName(String processName)
    {
        this.processName = processName;
    }


    @JSONProperty(ignore = true)
    public boolean isContainedInProcess()
    {
        return getProcessName() != null;
    }


    @JSONProperty(ignore = true)
    public String getLocalName()
    {
        if (processName == null)
        {
            throw new IllegalStateException("Cannot get local name from non-process view: " + this);
        }

        return getName().substring(processName.length() + 1);
    }


    /**
     * The view context model. Each property in it will exist within the view. Note that additional properties might
     * be added from the layout used.
     */
    public ContextModel getContextModel()
    {
        return contextModel;
    }


    @JSONProperty("context")
    public void setContextModel(ContextModel contextModel)
    {
        this.contextModel = contextModel;
    }


    public ViewState getViewState(RuntimeContext runtimeContext)
    {
        if (!isContainedInProcess())
        {
            return null;
        }

        return (ViewState) runtimeContext.getApplicationModel().getProcess(getProcessName()).getStates().get
            (getLocalName());
    }


    /**
     * UUID uniquely identifying the view. Doesn't need to be defined but needs to remain unchanged if it exists.
     *
     * @return
     */
    @Override
    public String getIdentityGUID()
    {
        return identityGUID;
    }


    @Override
    public void setIdentityGUID(String identity)
    {
        this.identityGUID = identity;
    }


    /**
     * Layout to use for this view. If not set, the application default layout will be used.
     */
    public String getLayout()
    {
        return layout;
    }


    public void setLayout(String layout)
    {
        this.layout = layout;
    }


    /**
     * Title of this view. Can be either a normal string attribute or an expression.
     */
    @DocumentedModelType("Expression")
    public String getTitle()
    {
        return title != null ? title.getValue() : null;
    }

    @JSONProperty(ignore = true)
    public ASTExpression getTitleExpression()
    {
        return title != null ? title.getAstExpression() : null;
    }

    @JSONProperty(ignore = true)
    public AttributeValue getTitleAttribute()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = AttributeValue.forValue(title, false);
    }


    public ComponentModel getContent(String name)
    {
        return content.get(name);
    }



}

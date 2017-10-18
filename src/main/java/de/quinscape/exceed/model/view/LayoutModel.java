package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.context.ContextModel;
import org.svenson.JSONProperty;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.component;

/**
 * Reusable top level layout definition model. The default layout for the application is defined in the application model.
 * Views can override the default by providing a layout prop to their root View component.
 */
public class LayoutModel
    extends AbstractTopLevelModel
{
    /**
     * Name of the component marking the place to insert the individual view's components.
     */
    public static final String CONTENT = "Content";

    /**
     * Default layout component structure which just invokes the "main" content
     */
    public static final ComponentModel DEFAULT_LAYOUT =
        component("Content")
            .withAttribute("name", View.MAIN)
            .getComponent();


    private ComponentModel root;

    private ContextModel contextModel;


    /**
     * Layout root component. When a view uses a layout, its root content gets replaced by the layout root content.
     *
     * The view starts rendering starts with the <code>"root"</code> content.
     *
     * At the position of each &lt;Content/&gt; component within, the corresponding content block from the view will be
     * rendered.
     */
    public ComponentModel getRoot()
    {
        return root;
    }


    public void setRoot(ComponentModel root)
    {
        this.root = root;
    }


    /**
     * Context model for the layout. The layout context is special in that it is a prototypic context. Each property
     * defined here will exists in every view the layout is used in without any connection between them.
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


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }
}

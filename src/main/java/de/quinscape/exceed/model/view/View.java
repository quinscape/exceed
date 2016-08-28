package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.ComponentViewContext;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.svenson.JSONProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class View
    extends TopLevelModel
    implements AutoVersionedModel
{
    private static final String VIEW_ROOT_COMPONENT = "View";

    private ComponentModel root;

    private List<String> comments;

    private boolean preview;

    private String processName;

    /**
     * cached JSON for the view itself. We need the view as view model, but we don't
     * want to to keep converting it back to json for the client,
     */
    private String cachedJSON;

    private String version;

    private ContextModel contextModel;


    /**
     * The root component of this view.
     *
     * @return
     */
    public ComponentModel getRoot()
    {
        return root;
    }


    public void setRoot(ComponentModel root)
    {
        if (!root.getName().equals(VIEW_ROOT_COMPONENT))
        {
            throw new IllegalStateException("Root component must be 'View'");
        }

        this.root = root;
    }


    public void setCachedJSON(String json)
    {
        this.cachedJSON = json;
    }


    @JSONProperty(ignore = true)
    public String getCachedJSON()
    {
        return cachedJSON;
    }


    public List<String> getComments()
    {
        return comments;
    }


    public void setComments(List<String> comments)
    {
        this.comments = comments;
    }


    @Override
    public String getVersion()
    {

        return version;
    }


    @Override
    public void setVersion(String version)
    {
        this.version = version;
    }


    public boolean isPreview()
    {
        return preview;
    }


    public void setPreview(boolean preview)
    {
        this.preview = preview;
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
        return root.find(predicate);
    }


    public String getProcessName()
    {
        return processName;
    }


    public void setProcessName(String processName)
    {
        this.processName = processName;
    }


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


    public void initContext()
    {
        contextModel = contextModelFromViewModel();
        createViewContextFromComponentsRecursively(getRoot());
    }


    private ContextModel contextModelFromViewModel()
    {
        final ContextModel contextModel = new ContextModel();
        for (ComponentModel kid : root.children())
        {
            if (kid.getName().equals("ViewContext"))
            {
                initFromComponents(contextModel, kid);
            }
        }
        return contextModel;
    }


    private void initFromComponents(ContextModel contextModel, ComponentModel viewContextComponent)
    {
        Map<String, ScopedListModel> lists = new HashMap<>();
        Map<String, ScopedObjectModel> objects = new HashMap<>();
        Map<String, ScopedPropertyModel> properties = new HashMap<>();

        for (ComponentModel model : viewContextComponent.children())
        {
            final String modelName = model.getName();
            switch (modelName)
            {
                case "List":
                {
                    final String name = model.getAttribute("name").getValue();
                    final String expr = model.getAttribute("queryExpression").getValue();
                    final ScopedListModel scopedListModel = new ScopedListModel();
                    scopedListModel.setName(name);
                    scopedListModel.setQueryExpression(expr);
                    lists.put(name, scopedListModel);
                    break;
                }
                case "Object":
                {
                    final AttributeValue nameAttr = model.getAttribute("name");
                    final AttributeValue typeAttr = model.getAttribute("type");
                    final AttributeValue defaultAttr = model.getAttribute("defaultValue");

                    if (nameAttr == null)
                    {
                        throw new IllegalStateException("<Object/> has no name attribute");
                    }
                    if (typeAttr == null)
                    {
                        throw new IllegalStateException("<Object/> has no type attribute");
                    }

                    final String name = nameAttr.getValue();
                    final String type = typeAttr.getValue();
                    final String defaultValue = defaultAttr != null ? defaultAttr.getValue() : null;

                    ScopedObjectModel scopedObjectModel = new ScopedObjectModel();
                    scopedObjectModel.setName(name);
                    scopedObjectModel.setType(type);
                    scopedObjectModel.setDefaultValue(defaultValue);
                    objects.put(name, scopedObjectModel);
                    break;
                }
                case "Property":
                {

                    final AttributeValue nameAttr = model.getAttribute("name");
                    final AttributeValue typeAttr = model.getAttribute("type");
                    final AttributeValue defaultAttr = model.getAttribute("defaultValue");

                    if (nameAttr == null)
                    {
                        throw new IllegalStateException("<Property/> has no name attribute");
                    }
                    if (typeAttr == null)
                    {
                        throw new IllegalStateException("<Property/> has no type attribute");
                    }

                    final String name = nameAttr.getValue();
                    final String type = typeAttr.getValue();
                    final String defaultValue = defaultAttr != null ? defaultAttr.getValue() : null;

                    ScopedPropertyModel scopedObjectModel = new ScopedPropertyModel();
                    scopedObjectModel.setName(name);
                    scopedObjectModel.setType(type);
                    scopedObjectModel.setDefaultValue(defaultValue);
                    properties.put(name, scopedObjectModel);
                    break;
                }
            }
        }

        contextModel.setLists(lists);
        contextModel.setObjects(objects);
        contextModel.setProperties(properties);
    }


    private void createViewContextFromComponentsRecursively(ComponentModel component)
    {
        if (component.isComponent())
        {
            ComponentDescriptor descriptor = component.getComponentRegistration().getDescriptor();
            ComponentViewContext componentViewContext = descriptor.getComponentViewContext();
            String id = component.getComponentId();

            if (componentViewContext != null && id != null)
            {
                if (componentViewContext.isList())
                {
                    HashMap<String, ScopedListModel> map = new HashMap<>();
                    final ScopedListModel model = new ScopedListModel();
                    map.put(id, model);
                    model.setQueryExpression(componentViewContext.getDefaultValue());
                    contextModel.setLists(map);
                }
                else if (componentViewContext.getObjectType() != null)
                {
                    Map<String, ScopedObjectModel> map = new HashMap<>();
                    ScopedObjectModel model = new ScopedObjectModel();
                    model.setType(componentViewContext.getObjectType());
                    model.setDefaultValue(componentViewContext.getDefaultValue());
                    map.put(id, model);

                    contextModel.setObjects(map);
                }
                else if (componentViewContext.getPropertyType() != null)
                {
                    Map<String, ScopedPropertyModel> map = new HashMap<>();
                    ScopedPropertyModel model = new ScopedPropertyModel();
                    model.setType(componentViewContext.getPropertyType());
                    model.setDefaultValue(componentViewContext.getDefaultValue());
                    map.put(id, model);
                    contextModel.setProperties(map);
                }
            }
        }

        for (ComponentModel kid : component.children())
        {
            createViewContextFromComponentsRecursively(kid);
        }
    }


    public ContextModel getContextModel()
    {
        return contextModel;
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
}

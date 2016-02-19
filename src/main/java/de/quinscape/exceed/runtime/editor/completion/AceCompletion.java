package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.model.view.ComponentModel;
import org.svenson.JSONProperty;

/**
 * The ace editor caption JSON format plus some more properties
 */
public class AceCompletion
{
    private final CompletionType type;

    private final String caption, meta, doc;

    private final ComponentModel componentModel;

    private final WizardInfo<?> wizardInfo;


    public AceCompletion(

        CompletionType type,
        String caption,
        String meta,
        String doc
    )
    {
        this(type, caption, meta, doc, null, null);
    }


    public AceCompletion(
        CompletionType type,
        String caption,
        String meta,
        String doc,
        ComponentModel componentModel,
        WizardInfo<?> wizardInfo
    )
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        this.type = type;
        this.caption = caption;
        this.meta = meta;
        this.doc = doc;
        this.componentModel = componentModel;
        this.wizardInfo = wizardInfo;
    }


    /**
     * Returns the ace editor completion caption property
     *
     * @return
     */
    public String getCaption()
    {
        return caption;
    }


    /**
     * Returns the ace editor completion meta property
     *
     * @return
     */
    public String getMeta()
    {
        return meta;
    }


    /**
     * Returns the ace editor completion doc property
     *
     * @return
     */
    public String getDoc()
    {
        return doc;
    }


    /**
     * Returns our type enum for the completion
     *
     * @return
     */
    public CompletionType getType()
    {
        return type;
    }


    /**
     * Gets the wizard info (our addition) will be used to determine if the completion is backed by a client side
     * wizard.
     *
     * @return
     */
    @JSONProperty("wizard")
    public WizardInfo<?> getWizardInfo()
    {
        return wizardInfo;
    }


    /**
     * Returns the component model for a completion if there is one.
     * @return
     */
    @JSONProperty("model")
    public ComponentModel getComponentModel()
    {
        return componentModel;
    }
}

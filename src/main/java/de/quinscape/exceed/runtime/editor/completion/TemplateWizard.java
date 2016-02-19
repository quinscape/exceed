package de.quinscape.exceed.runtime.editor.completion;

public class TemplateWizard
    implements WizardInfo<Long>
{
    private final Long key;


    public TemplateWizard(long key)
    {
        this.key = key;

    }


    @Override
    public Long getKey()
    {
        return key;
    }
}

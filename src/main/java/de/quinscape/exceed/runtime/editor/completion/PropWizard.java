package de.quinscape.exceed.runtime.editor.completion;

import org.svenson.JSONParameter;

public class PropWizard
    implements WizardInfo<String>
{
    private final String key;


    public PropWizard(
        @JSONParameter("key")
        String key
    )
    {
        this.key = key;
    }


    @Override
    public String getKey()
    {
        return key;
    }
}

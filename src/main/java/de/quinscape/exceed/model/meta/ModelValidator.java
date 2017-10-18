package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.model.ApplicationModel;

public interface ModelValidator
{
    void validate(ModelValidationContext ctx, ApplicationModel applicationModel);
}

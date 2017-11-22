package de.quinscape.exceed.model.annotation;

import de.quinscape.exceed.model.ApplicationModel;

public interface ResourceInjectorPredicate
{
    boolean shouldInject(ApplicationModel applicationModel);
}

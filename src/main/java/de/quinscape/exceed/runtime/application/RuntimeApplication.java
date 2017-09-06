package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.scope.ScopedContext;

/**
 * Encapsulates application wide information relevant to a large number of submodules.
 *
 * @see DefaultRuntimeApplication
 */
public interface RuntimeApplication
{
    /** runtime info meta data name. cannot be used as component id */
    String RUNTIME_INFO_NAME = "_exceed";

    default String getName()
    {
        return getApplicationModel().getName();
    }

    ApplicationModel getApplicationModel();

    ScopedContext getApplicationContext();

    ResourceLoader getResourceLoader();

    RuntimeContext createSystemContext();
}

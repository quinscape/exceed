package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.resource.ResourceLoader;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates application wide information relevant to a large number of submodules.
 *
 * @see DefaultRuntimeApplication
 */
public interface RuntimeApplication
{
    ServletContext getServletContext();

    ApplicationModel getApplicationModel();

    DomainService getDomainService();
}

package de.quinscape.exceed.model.view;

import java.util.List;

/**
 * Implemented by sources of components for a {@link ComponentModelBuilder} build process.
 */
interface ComponentSource
{
    /**
     * Produces a list of component models for the current component source
     * 
     * @return list of component models
     */
    List<ComponentModel> getComponents();
}

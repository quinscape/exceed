package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import org.svenson.JSONParameter;

import java.util.List;

public class ComponentPath
{
    private final String content;
    private final List<Long> path;


    public ComponentPath(
        @JSONParameter("content")
        String content,
        @JSONParameter("path")
        List<Long> path
    )
    {
        this.content = content;
        this.path = path;
    }


    public String getContent()
    {
        return content;
    }


    public List<Long> getPath()
    {
        return path;
    }


    /**
     * Locate the component model described by this path in the given view model.
     *
     * @param viewModel     view model
     * @return  component model
     *
     * @throws ComponentTraversalException if the path in invalid for the view
     */
    public ComponentModel walk(View viewModel)
    {
        ComponentModel componentModel = viewModel.getContent(content);
        if (componentModel == null)
        {
            throw new ComponentTraversalException("No content '" + content + "' in " + viewModel);
        }

        for (int i = 0 ; i < path.size(); i++)
        {
            componentModel = componentModel.getKids().get(path.get(i).intValue());
        }
        return componentModel;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "content = '" + content + '\''
            + ", path = " + path
            ;
    }
}

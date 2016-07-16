package de.quinscape.exceed.runtime.expression.component;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.controller.ActionService;

import java.util.Set;

/**
 * Created by sven on 18.03.16.
 */
public class TestActionService
    implements ActionService
{
    @Override
    public Set<String> getActionNames()
    {
        return ImmutableSet.of("foo", "bar", "baz");
    }


    @Override
    public Action getAction(String name)
    {
        return (runtimeContext, Model) -> true;
    }
}

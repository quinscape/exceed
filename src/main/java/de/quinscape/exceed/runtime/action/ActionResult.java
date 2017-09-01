package de.quinscape.exceed.runtime.action;

public interface ActionResult
{
    boolean isResolved();

    Object toJS();
    Object toJSON();
    Object get();
}

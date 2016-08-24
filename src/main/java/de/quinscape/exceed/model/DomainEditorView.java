package de.quinscape.exceed.model;

import java.util.Map;

/**
 * Encapsulates position data for one named view to the application domain. Each view has its own independent entity
 * visibility and entity object positions.
 */
public class DomainEditorView
{
    private Map<String, Boolean> visible;

    private Map<String, Position> positions;


    public Map<String, Boolean> getVisible()
    {
        return visible;
    }


    public void setVisible(Map<String, Boolean> visible)
    {
        this.visible = visible;
    }


    public Map<String, Position> getPositions()
    {
        return positions;
    }


    public void setPositions(Map<String, Position> positions)
    {
        this.positions = positions;
    }
}

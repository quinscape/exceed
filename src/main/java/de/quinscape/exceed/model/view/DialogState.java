package de.quinscape.exceed.model.view;

/**
 * Formal model for the dialog state object that controls the display of each dialog component
 */
public class DialogState
{
    private boolean isOpen;

    private String title;


    public String getTitle()
    {
        return title;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }


    public boolean isOpen()
    {
        return isOpen;
    }


    public void setOpen(boolean open)
    {
        isOpen = open;
    }
}

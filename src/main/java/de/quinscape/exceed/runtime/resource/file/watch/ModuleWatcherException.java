package de.quinscape.exceed.runtime.resource.file.watch;


import de.quinscape.exceed.runtime.ExceedRuntimeException;

/**
 * Thrown when watcher initialization fails.
 */
public class ModuleWatcherException
    extends ExceedRuntimeException
{

    private static final long serialVersionUID = -8152488876433384555L;

    public ModuleWatcherException(String message)
    {
        super(message);
    }

    public ModuleWatcherException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModuleWatcherException(Throwable cause)
    {
        super(cause);
    }
}

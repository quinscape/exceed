package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Encapsulates the kind of change that happened to a resource.
 */
public enum ModuleResourceEvent
{
    CREATED, MODIFIED, DELETED;

    public static ModuleResourceEvent forWatchEvent(WatchEvent event)
    {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE))
        {
            return CREATED;
        }
        else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE))
        {
            return DELETED;
        }
        else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
        {
            return MODIFIED;
        }
        throw new ExceedRuntimeException("Cannot convert event into module resource event" + event);
    }
}

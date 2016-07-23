package de.quinscape.exceed.runtime.resource.file.watch;

import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements a module watcher based on Java7 watch services.
 *
 */
public class Java7NIOResourceWatcher
    extends WatchDir
    implements ResourceWatcher, Runnable
{
    private final static Logger log = LoggerFactory.getLogger(Java7NIOResourceWatcher.class);

    private final FileResourceRoot root;

    private final int basePathLength;

    private CopyOnWriteArrayList<ResourceChangeListener> listeners = new CopyOnWriteArrayList<>();

    public Java7NIOResourceWatcher(FileResourceRoot root) throws IOException
    {
        super(root.getBaseDirectory().getPath(), true);
        this.root = root;
        this.basePathLength = root.getBaseDirectory().getPath().length();
    }

    public void start()
    {
        log.info("Start watcher for {}", root);

        Thread t = new Thread(this, "Watcher-" + root.getBaseDirectory().getPath());
        t.setDaemon(true);
        t.start();
    }


    @Override
    public void run()
    {
        processEvents();
    }

    @Override
    protected void onWatchEvent(WatchEvent<?> event, File child)
    {
        String path = child.getPath();
        ModuleResourceEvent resourceEvent = ModuleResourceEvent.forWatchEvent(event);
        final String resourcePath = path.substring(basePathLength);
        for (ResourceChangeListener listener : listeners)
        {
            log.debug("Signaling {} for {} ({})", listener, path, resourceEvent);

            listener.onResourceChange(resourceEvent, root, resourcePath);
        }
    }


    @Override
    public void register(ResourceChangeListener listener)
    {
        log.debug("Register {}", listener);

        listeners.add(listener);
    }


    @Override
    public void clearListeners()
    {
        log.debug("Clear all listeners");
        listeners.clear();
    }
}

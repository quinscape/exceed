package de.quinscape.exceed.runtime.resource.file.watch;

import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.util.Util;
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
public class Java7NIOModuleWatcher
    extends WatchDir
    implements ModuleWatcher, Runnable
{
    private static Logger log = LoggerFactory.getLogger(Java7NIOModuleWatcher.class);

    private final File baseDir;
    private final boolean jsxAllowed;
    private CopyOnWriteArrayList<ModuleWatcherListener> listeners = new CopyOnWriteArrayList<ModuleWatcherListener>();

    public Java7NIOModuleWatcher(File baseDir, boolean jsxAllowed) throws IOException
    {
        super(baseDir.getPath(), true);
        this.baseDir = baseDir;
        this.jsxAllowed = jsxAllowed;
    }

    public void start()
    {
        log.info("Start watcher for {}", baseDir);

        Thread t = new Thread(this, "Exceed resource watcher:" + baseDir.getPath());
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void register(ModuleWatcherListener listener)
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

    @Override
    public void run()
    {
        processEvents();
    }

    @Override
    protected void onWatchEvent(WatchEvent<?> event, File child)
    {
        String fileName = child.getPath();
        signalResourceChange(event,fileName);
    }

    private void signalResourceChange(WatchEvent<?> event, final String modulePath)
    {
        for (ModuleWatcherListener listener : listeners)
        {
            String appBase = listener.getListenerBase();
            String appRoot;
            if (appBase.length() == 0)
            {
                appRoot = baseDir.getPath();
            }
            else
            {
                appRoot = new File(baseDir, Util.path(appBase)).getPath();
            }

            if (modulePath.startsWith(appRoot))
            {
                String inAppPath = modulePath.substring(appRoot.length() + 1);

                ModuleResourceEvent resourceEvent = ModuleResourceEvent.forWatchEvent(event);

                log.debug("Signaling {} for {}", resourceEvent, inAppPath);

                listener.onModuleChange(resourceEvent, inAppPath);
            }
            else
            {
                log.trace("Ignoring {} (does not start with {})", modulePath, appRoot);
            }
        }
    }
}

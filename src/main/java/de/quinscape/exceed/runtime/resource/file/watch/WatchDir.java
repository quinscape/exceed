package de.quinscape.exceed.runtime.resource.file.watch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.nio.file.LinkOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

abstract class WatchDir
{
    private static Logger log = LoggerFactory.getLogger(WatchDir.class);

    private final WatchService watcher;
    private final ConcurrentMap<WatchKey, Path> keys;
    private final boolean recursive;
    private volatile boolean running = true;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event)
    {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException
    {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(String path, boolean recursive) throws IOException
    {
        Path dir = FileSystems.getDefault().getPath(path);

        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new ConcurrentHashMap<>();
        this.recursive = recursive;

        if (recursive)
        {
            log.trace("Scanning %s ...\n", dir);
            registerAll(dir);
            log.trace("Done.");
        }
        else
        {
            register(dir);
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */
    protected void processEvents()
    {
        while (running)
        {

            // wait for key to be signalled
            WatchKey key;
            try
            {
                key = watcher.take();
            }
            catch (InterruptedException x)
            {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null)
            {
                log.error("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents())
            {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW)
                {
                    log.info("Event overflow");
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                try
                {
                    onWatchEvent(event, child.toAbsolutePath().toFile());
                }
                catch(RuntimeException e)
                {
                    log.info("Error processing watch event", e);
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE))
                {
                    try
                    {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS))
                        {
                            registerAll(child);
                        }
                    }
                    catch (IOException x)
                    {
                        throw new ResourceWatcherException(x);
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid)
            {
                keys.remove(key);

//                // all directories are inaccessible
//                if (keys.isEmpty())
//                {
//                    break;
//                }
            }
        }
    }

    protected abstract void onWatchEvent(WatchEvent<?> event, File child);

    public void shutDown()
    {
        running = false;
    }
}

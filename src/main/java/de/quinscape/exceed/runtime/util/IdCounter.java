package de.quinscape.exceed.runtime.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Produces base32 encoded ids based on an atomic long counter.
 *
 */
public class IdCounter
{
    private final String prefix;

    private final AtomicLong counter;

    public IdCounter()
    {
        this(null);
    }

    public IdCounter(String prefix)
    {
        this.prefix = prefix;
        this.counter = new AtomicLong();
    }

    public String nextId()
    {
        String id = Util.base32(counter.incrementAndGet());
        return prefix != null ? prefix + id : id;
    }
}

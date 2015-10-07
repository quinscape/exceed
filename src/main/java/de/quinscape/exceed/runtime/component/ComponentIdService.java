package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.util.Util;

import java.util.concurrent.atomic.AtomicLong;

public class ComponentIdService
{
    private AtomicLong idCounter = new AtomicLong(0L);

    public String createId()
    {
        return "id-" + Util.base32(idCounter.incrementAndGet());
    }
}

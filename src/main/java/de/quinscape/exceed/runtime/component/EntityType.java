package de.quinscape.exceed.runtime.component;

import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum EntityType
{
    ENTITY,
    DOMAIN_MODEL,
    CUSTOM;

    private final static Set<String> names = ImmutableSet.copyOf(
        Arrays.stream(values())
            .map(EntityType::name)
            .collect(Collectors.toSet())
    );

    public static EntityType fromJSON(String value)
    {
        if (names.contains(value))
        {
            return EntityType.valueOf(value);
        }
        return EntityType.CUSTOM;
    }
}

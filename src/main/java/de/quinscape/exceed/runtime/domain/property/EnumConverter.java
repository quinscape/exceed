package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;
import java.util.Map;

public class EnumConverter
    extends NullConverter<Long>
{
    public EnumConverter()
    {
        super(Long.class);
    }
}

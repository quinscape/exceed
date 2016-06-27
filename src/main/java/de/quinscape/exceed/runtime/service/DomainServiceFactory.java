package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.config.DefaultPropertyConverters;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Creates domain services at runtime. Each runtime application has its own domain service.
 */
public class DomainServiceFactory
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceFactory.class);

    private final Map<String, PropertyConverter> converters;

    private final NamingStrategy namingStrategy;

    private final DSLContext dslContext;


    public DomainServiceFactory(DefaultPropertyConverters defaultPropertyConverters)


    {
        this.namingStrategy = namingStrategy;
        this.dslContext = dslContext;
        this.converters = defaultPropertyConverters.getConverters();
    }


    public DomainService create()
    {
        return new DomainServiceImpl(dslContext, namingStrategy, converters);
    }

}

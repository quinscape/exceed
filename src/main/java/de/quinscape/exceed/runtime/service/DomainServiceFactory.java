package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.datalist.DataListService;
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

    private final DataListService dataListService;

    private final NamingStrategy namingStrategy;

    private final DSLContext dslContext;

    private final Map<String, PropertyConverter> converters;


    public DomainServiceFactory(DataListService dataListService, NamingStrategy namingStrategy, DSLContext dslContext, Map<String, PropertyConverter> converters)
    {
        this.dataListService = dataListService;
        this.namingStrategy = namingStrategy;
        this.dslContext = dslContext;
        this.converters = converters;
    }


    public DomainService create()
    {
        return new DomainServiceImpl(dataListService, dslContext, namingStrategy, converters);
    }

}

package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.datalist.DataListService;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates domain services at runtime. Each runtime application has its own domain service.
 */
@Service
public class DomainServiceFactory
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceFactory.class);

    @Autowired
    private DataListService dataListService;

    public DomainService create()
    {
        return new DomainServiceImpl(dataListService);
    }

}

package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates domain services at runtime. Each runtime application has its own domain service.
 */
public class DomainServiceFactory
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceFactory.class);

    private final StorageConfigurationRepository storageConfigurationRepository;


    public DomainServiceFactory(
        StorageConfigurationRepository storageConfigurationRepository
    )
    {
        this.storageConfigurationRepository = storageConfigurationRepository;
    }


    public DomainService create()
    {
        return new DomainServiceImpl(storageConfigurationRepository);
    }

}

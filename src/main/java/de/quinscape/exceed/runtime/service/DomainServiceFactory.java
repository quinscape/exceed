package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Creates domain services at runtime. Each runtime application has its own domain service.
 */
@Service
public class DomainServiceFactory
    implements ApplicationContextAware

{
    private static Logger log = LoggerFactory.getLogger(DomainServiceFactory.class);


    @Autowired
    private DSLContext dslContext;

    private ApplicationContext applicationContext;

    private Map<String, PropertyConverter> propertyTypes;

    public DomainService create()
    {
        return new DomainServiceImpl(propertyTypes);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;

        propertyTypes = applicationContext.getBeansOfType(PropertyConverter.class);

    }
}

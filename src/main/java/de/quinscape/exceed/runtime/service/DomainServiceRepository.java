package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.domain.DomainService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentMap;

/**
 * Provides access to per-application domain services
 */
@Service
public class DomainServiceRepository
{
    private ConcurrentMap<String,DomainService> allServices;

    public DomainService getDomainService(String appName)
    {
        return allServices.get(appName);
    }

    public void register(String appName, DomainService domainService)
    {
        allServices.put(appName, domainService);
    }
}

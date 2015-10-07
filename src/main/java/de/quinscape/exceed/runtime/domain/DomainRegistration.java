package de.quinscape.exceed.runtime.domain;

public interface DomainRegistration
{
    /**
     * Registers domain types using the given {@link DomainRegistry}.
     *
     * @param registry  domain type registry
     */
    void register(DomainRegistry registry);
}

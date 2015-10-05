package de.quinscape.exceed.runtime.domain;

public interface DomainTypeRegistration
{
    /**
     * Registers domain types using the given {@link DomainTypeRegistry}.
     *
     * @param registry  domain type registry
     */
    void register(DomainTypeRegistry registry);
}

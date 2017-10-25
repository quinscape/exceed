package de.quinscape.exceed.runtime.domain.migration;

import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.MigrationStepModel;

public interface MigrationStep<T extends MigrationStepModel>
{
    String STATUS_OK = "ok";

    String describe(T t);

    String check(DomainVersion domainVersion, T t);

    void apply(DomainVersion domainVersion, T t);
}

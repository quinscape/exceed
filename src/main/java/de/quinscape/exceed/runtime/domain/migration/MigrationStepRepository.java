package de.quinscape.exceed.runtime.domain.migration;

import java.util.Map;

public class MigrationStepRepository
{
    private final Map<String,MigrationStep> migrationSteps;


    public MigrationStepRepository(Map<String, MigrationStep> migrationSteps)
    {
        this.migrationSteps = migrationSteps;
    }


    public MigrationStep getMigrationStep(String name)
    {
        final MigrationStep<?> migrationStep = migrationSteps.get(name);

        if (migrationStep == null)
        {
            throw new IllegalStateException("No migration bean '" + name + "' found.");
        }

        return migrationStep;
    }
}

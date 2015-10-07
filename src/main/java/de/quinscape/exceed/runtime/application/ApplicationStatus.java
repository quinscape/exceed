package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.domain.tables.pojos.AppState;

public enum ApplicationStatus
{
    OFFLINE
        {
            @Override
            public boolean hasValidTransitionTo(ApplicationStatus status)
            {
                return status == PREVIEW || status == PRODUCTION;
            }
        },
    PRODUCTION
        {
            @Override
            public boolean hasValidTransitionTo(ApplicationStatus status)
            {
                return status == OFFLINE;
            }
        },
    PREVIEW
        {
            @Override
            public boolean hasValidTransitionTo(ApplicationStatus status)
            {
                return status == OFFLINE;
            }
        };

    public abstract boolean hasValidTransitionTo(ApplicationStatus status);

    public static ApplicationStatus from(AppState state)
    {
        return ApplicationStatus.values()[state.getStatus()];
    }
}

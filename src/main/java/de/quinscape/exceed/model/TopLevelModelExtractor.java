package de.quinscape.exceed.model;

public interface TopLevelModelExtractor<T extends TopLevelModel>
{
    TopLevelModel get(ApplicationModel applicationModel, String name);
}

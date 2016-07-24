package de.quinscape.exceed.model.domain;

import org.springframework.util.StringUtils;

public abstract class MigrationStepModel
{
    private final String beanName;

    private String appName;

    public MigrationStepModel(String beanName)
    {
        if (!StringUtils.hasText(beanName))
        {
            throw new IllegalArgumentException(beanName + " must  not be empty or null");
        }
        this.beanName = beanName;
    }


    public String getBeanName()
    {
        return beanName;
    }


    public String getAppName()
    {
        return appName;
    }


    public void setAppName(String appName)
    {
        this.appName = appName;
    }
}

package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;

import javax.servlet.ServletContext;

public interface RuntimeApplicationFactory
{
    DefaultRuntimeApplication createRuntimeApplication(ServletContext servletContext, AppState state);
}

package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.util.AppAuthentication;

import javax.servlet.http.HttpSession;

public interface MessageHubRegistry
{
    String registerConnection(HttpSession session, RuntimeApplication application, AppAuthentication auth);
}

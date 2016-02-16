package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.util.AppAuthentication;

import javax.servlet.http.HttpSession;

public interface EditorMessageHub
{
    void register(String id, AppAuthentication auth, HttpSession session, RuntimeApplication appName);

    void send(String id, Object message);
}

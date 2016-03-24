package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.AppAuthentication;

import javax.servlet.http.HttpSession;

public interface EditorMessageHub
{
    void register(String id, AppAuthentication auth, HttpSession session, RuntimeContext runtimeContext);

    void send(String id, Object message);
}

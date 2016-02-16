package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.message.IncomingMessage;

/**
 * Handler interface for the exceed websocket message infrastructure.
 *
 * The [@link #getMessageType) method defines the Java type of messages received
 * this handler will be called.
 *
 * The client can call these handlers by providing the correct type attribute on
 * the incoming data.
 *
 *  <pre>
 *      var hub = require("exceed-services").Hub;
 *
 *      hub.request({
 *          type: "message.Foo"
 *      })
 *  </pre>
 *
 *  The server looks up the handler by name. The name of a message handler is built
 *  by taking the "message." prefix and adding the simple class name of the
 *  message type implementation corresponding to the handler.
 *
 *  <p>
 *      The websocket server will convert the incoming JSON messages to the message type and
 *      obey svenson JSON annotations etc in the message type implementations. It will also
 *      parse models into their correct Java type based on the same type attribute, but with
 *      a valid model type.
 *  </p>
 *
 * The message handler implementations are looked up in the spring application context. Make sure to define custom handlers as beans.
 *
 * You can automate this by using @ComponentScan like this

    <pre>
        @ComponentScan(
            value = {
                "your.package.to.scan",
            },
            includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EditorMessageHandler.class)
            }
        )

        @Configuration
        public class WebConfiguration
        {
            ...
        }

    </pre>

    <p>
        This will cause Spring beans to be created for all EditorMessageHandler implementations in <code>your.package.to.scan</code>
    </p>

 * @param <T>   incoming message type that will be handled by this message handler
 */
public interface EditorMessageHandler<T extends IncomingMessage>
{
    void handle(MessageContext context, T msg) throws Exception;

    /**
     * Returns the message type handled by this handler.
     *
     * @return  message type
     */
    Class<T> getMessageType();
}

package de.quinscape.exceed.runtime.editor.domain;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.editor.completion.PropCompleteQuery;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.FileAppResource;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;

public class SaveViewHandler
    implements EditorMessageHandler<SaveViewRequest>
{

    @Override
    public void handle(MessageContext context, SaveViewRequest msg) throws Exception
    {
        String viewName = msg.getViewName();

        if (viewName == null)
        {
            throw new IllegalArgumentException("viewName can't be null");
        }

        RuntimeApplication runtimeApplication = context.getRuntimeContext().getRuntimeApplication();

        View view = runtimeApplication.getApplicationModel().getViews().get(viewName);

        AppResource resource = view.getResource();

        if (resource instanceof FileAppResource)
        {
            FileUtils.writeStringToFile(((FileAppResource) resource).getFile(), msg.getJson(), RequestUtil.UTF_8);
            context.reply(msg, true);
        }
        else
        {
            context.reply(msg, new Error("Cannot write resource " + resource));
        }
    }


    @Override
    public Class<SaveViewRequest> getMessageType()
    {
        return SaveViewRequest.class;
    }
}

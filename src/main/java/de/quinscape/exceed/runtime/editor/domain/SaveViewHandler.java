package de.quinscape.exceed.runtime.editor.domain;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.FileAppResource;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.apache.commons.io.FileUtils;

import java.util.Map;

public class SaveViewHandler
    implements EditorMessageHandler<SaveViewRequest>
{

    @Override
    public void handle(MessageContext context, SaveViewRequest msg) throws Exception
    {
        for (Map.Entry<String, String> entry : msg.getDocuments().entrySet())
        {
            final String viewName = entry.getKey();
            final String json = entry.getValue();

            if (viewName == null)
            {
                throw new IllegalArgumentException("viewName can't be null");
            }

            RuntimeApplication runtimeApplication = context.getRuntimeContext().getRuntimeApplication();

            View view = runtimeApplication.getApplicationModel().getView(viewName);

            AppResource resource = view.getResource();

            if (resource.isWritable())
            {
                FileUtils.writeStringToFile(((FileAppResource) resource).getFile(), json, RequestUtil.UTF_8);
            }
            else
            {
                context.reply(msg, new Error("Cannot write resource " + resource));
                return;
            }
        }
        context.reply(msg, true);
    }


    @Override
    public Class<SaveViewRequest> getMessageType()
    {
        return SaveViewRequest.class;
    }
}
